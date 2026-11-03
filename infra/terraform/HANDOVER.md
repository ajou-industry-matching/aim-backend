# Terraform 인수인계 (운영/주의 중심)

이 문서는 `infra/terraform`를 처음 맡는 사람을 위한 **운영 절차와 함정** 정리다.
각 리소스가 무엇을 만드는지, Workload Identity 설정, GitHub Variables 등록 등 **구성 설명은
[`README.md`](./README.md)에 있으니 중복하지 않는다.** 여기서는 "어떻게 돌리고, 무엇을 조심하나"만 다룬다.

---

## 1. 이 Terraform이 관리하는 것 (한눈에)

`aim-backend`의 Cloud Run **운영 기반(baseline)**. 앱 이미지 배포는 GitHub Actions가 하고,
TF는 그 아래 토대만 관리한다.

- Artifact Registry Docker repo (`app-repo`)
- 서비스 계정 2개: 배포용(`github-actions-deployer`), 런타임용(`aim-backend-runtime`)
- GitHub OIDC Workload Identity Pool/Provider (키 없는 배포)
- Cloud Run v2 서비스 `aim-be-prod` (asia-northeast3) baseline
- Secret Manager **컨테이너**: `aim-backend-firebase-adminsdk`, `aim-be-db-password`, `DB_URL`, `CRAWLER_API_KEY`
  (※ secret **값**은 TF가 넣지 않는다. 아래 3·5절 참고)
- 위 리소스에 대한 IAM

리소스별 상세는 README의 "관리 대상" 참고.

---

## 2. 상태(state) 백엔드 — ⚠️ 로컬이다

`versions.tf`/`providers.tf`에 **`backend` 블록이 없다.** 즉 tfstate는 원격(GCS 등)이 아니라
**apply를 실행한 사람의 로컬 디렉터리(`terraform.tfstate`)에만 존재**한다.

함정:
- state 파일이 특정 사람 머신에만 있으면 다른 사람은 그대로 이어받아 apply할 수 없다.
- 두 사람이 각자 apply하면 state가 갈라진다.
- 그래서 **apply는 state를 쥔 한 사람이, 조율해서** 수행해야 한다.

개선안(별도 작업): GCS 백엔드 도입 후 `terraform init -migrate-state`. 이 PR 범위는 아니다.

---

## 3. 사전 준비

1. **gcloud 인증**: `gcloud auth application-default login` (TF google provider가 ADC를 사용).
   프로젝트 Owner/Editor 수준 또는 최소한 Cloud Run·IAM·Secret Manager·Artifact Registry 관리 권한 필요.
2. **변수 공급**: 대부분 `variables.tf`에 default가 있어 추가 입력이 적다.
   default가 없어 **반드시 넘겨야 하는 값은 두 개**다.
   - `project_id` — GCP 프로젝트 ID
   - `bootstrap_image` — 서비스 최초 생성/첫 import 시 쓰는 임시 이미지 태그
   `-var=...`로 주거나 `terraform.tfvars`에 적는다. (예시는 README "실행 예시".)
3. **시크릿 값은 TF에 적지 않는다.** TF는 시크릿 **컨테이너**만 만들고, 실제 값은
   배포 워크플로가 GitHub Secrets에서 동기화한다(4절). 시크릿 값/접근 경로는
   레포의 시크릿 인수인계 가이드(`docs/`의 secrets 핸드오버 문서)를 참조하고, **여기에 값을 적지 않는다.**

---

## 4. Terraform vs gcloud 배포 워크플로 — 역할 분담

| | Terraform (`infra/terraform`) | 배포 워크플로 (`.github/workflows/deploy-cloud-run.yml`) |
|---|---|---|
| 관리 대상 | 기반 리소스/IAM/시크릿 컨테이너 baseline | 새 **이미지 revision** 배포 + env/secret **값** 주입 |
| 트리거 | 사람이 수동 `apply` | `main` push / 수동 dispatch |
| 이미지 | `bootstrap_image`로 최초 1회만 | 매 배포마다 `:<commit SHA>` 푸시·배포 |
| 시크릿 값 | **넣지 않음** (컨테이너만) | `DB_URL`/`DB_PASSWORD`/`FIREBASE_CREDENTIALS_JSON`/`CRAWLER_API_KEY`를 GitHub Secrets에서 Secret Manager로 생성·갱신 후 서비스에 주입 |

**핵심:** TF는 "토대", 워크플로는 "그 위의 배포". 두 도구가 같은 Cloud Run 서비스를 건드리므로
경계가 어긋나면 서로의 변경을 되돌린다(→ 6절 드리프트).

### `ignore_changes`의 의미
`cloud_run.tf`는 `lifecycle { ignore_changes = [template[0].containers[0].image] }`를 둔다.
→ 워크플로가 배포한 최신 이미지를 **다음 `apply`가 bootstrap 이미지로 되돌리지 않는다.**
**단, `image`만 무시한다.** env/secret 집합은 무시하지 않으므로, TF 선언과 라이브가 다르면
**apply가 env/secret을 TF 쪽으로 덮어쓴다.** 이게 아래 드리프트의 근본 원인이다.

---

## 5. 기본 워크플로

```bash
cd infra/terraform
terraform init
terraform fmt -check
terraform validate
terraform plan  -var="project_id=<PROJECT_ID>" -var="bootstrap_image=<...>"
# plan 검토 후에만
terraform apply -var="project_id=<PROJECT_ID>" -var="bootstrap_image=<...>"
```

최초 생성 시 서비스/시크릿이 이미 존재하면 **import 먼저**(6절·README 참고).

---

## 6. ⚠️ 반드시 알 것 — 드리프트와 apply 전 주의

Cloud Run 서비스는 지금 **TF와 배포 워크플로가 함께** 소유한다. 워크플로(#55/#64)가
서비스의 env/secret을 `overwrite` 전략으로 "권위 있게" 덮어쓰기 때문에, **라이브 서비스의
실제 구성과 TF 선언이 여러 군데 다르다.** `image` 외에는 `ignore_changes`가 없으므로
**아무 생각 없이 `terraform apply`하면 라이브 구성을 TF 쪽으로 되돌린다.**

### 6-1. 이 PR이 정합시킨 것 (DB_URL, CRAWLER_API_KEY)
- `DB_URL`: 과거 TF는 plaintext env(`value = var.db_url`)로 두어, apply 시 라이브의 Secret Manager
  주입을 **plaintext로 되돌리는** 위험이 있었다. → 이제 `db_password`와 동일하게
  `value_source.secret_key_ref`(시크릿 `DB_URL`)로 선언.
- `CRAWLER_API_KEY`: TF엔 아예 없어 apply 시 **제거**될 뻔했다. → 시크릿 `CRAWLER_API_KEY` ref로 추가.
- 두 시크릿은 라이브와 **같은 이름**(`DB_URL`, `CRAWLER_API_KEY`)으로 선언했다.
  **이미 워크플로가 만들어 둔 시크릿이므로, 첫 apply 전에 반드시 import**:

  ```bash
  terraform import 'google_secret_manager_secret.db_url' \
    'projects/<PROJECT_ID>/secrets/DB_URL'

  terraform import 'google_secret_manager_secret.crawler_api_key' \
    'projects/<PROJECT_ID>/secrets/CRAWLER_API_KEY'
  ```

  (import하지 않으면 apply가 같은 이름 시크릿을 **새로 만들려다 실패**한다.)
  런타임 SA의 `secretAccessor` IAM(`google_secret_manager_secret_iam_member.*`)은 가산형이라
  이미 부여돼 있어도 apply가 no-op으로 지나가 별도 import는 필요 없다.

### 6-2. 아직 남은 드리프트 (이 PR 범위 밖 — 후속 필요)
> **이 PR을 머지해도 `terraform apply`가 완전히 안전해지지는 않는다.** 아래는 여전히 라이브와 다르다.

1. **DB_PASSWORD 시크릿 이름 불일치**: TF는 `aim-be-db-password`를 참조하지만, 워크플로는
   시크릿 `DB_PASSWORD`를 만들어 라이브 서비스에 주입한다. → apply 시 TF가 참조를
   `aim-be-db-password`로 바꿔 라이브와 달라진다. (`aim-be-db-password`는 사실상 미사용 상태가 됐다.)
2. **FIREBASE 주입 방식 불일치**: TF는 시크릿 `aim-backend-firebase-adminsdk`를 **볼륨 파일**
   (`/secrets/firebase-adminsdk.json` + `FIREBASE_CREDENTIALS_PATH`)로 마운트한다. 반면 라이브는
   워크플로가 시크릿 `FIREBASE_CREDENTIALS_JSON`을 **env 변수**로 주입한다(볼륨 아님).
   → apply 시 주입 방식 자체가 달라진다.
3. **SPRING_FLYWAY_* 누락**: 워크플로는 `SPRING_FLYWAY_ENABLED`/`_BASELINE_ON_MIGRATE`/
   `_BASELINE_VERSION`을 env로 주입하지만 TF엔 없다. → apply 시 **제거**된다.

위 3가지를 TF에 완전 반영하려면 시크릿 import/이름 재정렬, firebase를 env 방식으로 전환,
Flyway env 추가가 필요하며, firebase는 앱의 실제 로딩 방식 확인이 선행돼야 한다(별도 PR 권장).

### 6-3. apply 전 체크리스트
- [ ] 6-1의 `terraform import` 2건을 먼저 실행했는가
- [ ] `terraform plan`에서 **`DB_URL`이 plaintext→secret으로** 바뀌는지 확인 (라이브와 일치시키는 변화라 정상)
- [ ] plan에 6-2 외 **예상치 못한** 서비스 계정/IAM/ingress/스케일 변경이 없는지 확인
- [ ] state를 쥔 사람이 단독으로, 배포 워크플로가 도는 시점과 겹치지 않게 조율해 실행
- [ ] 6-2 드리프트가 그대로 보이면, 그 영향(FLYWAY 제거·firebase 방식 변경 등)을 팀이 인지한 상태에서만 apply
