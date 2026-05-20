# GitHub Actions -> Cloud Run CI/CD 운영 문서

## 브랜치 전략

- 기본 브랜치: `main`
- 개발 브랜치: `feature/*`
- 병합 흐름: `feature/*`에서 `main`으로 Pull Request 생성
- PR 하단 Checks 영역에 `CI / Gradle test and build` 결과가 표시된다.
- `main`에 merge되면 `Deploy to Cloud Run` workflow가 실행된다.

## GitHub Actions

### CI

파일: `.github/workflows/ci.yml`

`pull_request`가 `main`을 대상으로 열리거나 갱신될 때 실행된다.

검증 내용:

- Java 17 설정
- Gradle cache 설정
- `./gradlew test bootJar --no-daemon`

현재 저장소에는 별도 테스트 코드가 없으므로, 이 단계는 우선 컴파일과 Gradle test task 성공 여부를 보장한다. 테스트가 추가되면 같은 workflow가 회귀 테스트 gate 역할을 한다.

### Deploy

파일: `.github/workflows/deploy-cloud-run.yml`

`main` push 또는 수동 `workflow_dispatch`에서 실행된다.

job 구조:

- `verify`: CI와 같은 `./gradlew test bootJar --no-daemon` 실행
- `deploy`: `needs: verify`로 연결되어 검증 실패 시 실행되지 않음

배포 내용:

- GitHub OIDC와 Google Workload Identity Federation으로 GCP 인증
- Artifact Registry Docker auth 설정
- Docker image build
- Artifact Registry push
- Cloud Run revision 배포

이미지 태그:

```text
<REGION>-docker.pkg.dev/<PROJECT_ID>/<REPOSITORY>/<IMAGE_NAME>:<GITHUB_SHA>
```

## GitHub Variables

Repository Settings > Secrets and variables > Actions > Variables에 등록한다.

| 이름 | 예시 | 설명 |
| --- | --- | --- |
| `GCP_PROJECT_ID` | `my-project` | Google Cloud project ID |
| `GCP_REGION` | `asia-northeast3` | Cloud Run 및 Artifact Registry region |
| `CLOUD_RUN_SERVICE` | `aim-be` | Cloud Run service name |
| `ARTIFACT_REGISTRY_REPOSITORY` | `app-repo` | Artifact Registry repository ID |
| `IMAGE_NAME` | `aim-be` | Docker image name |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | `projects/123456789/locations/global/workloadIdentityPools/aim-backend-github/providers/github` | Terraform output `github_workload_identity_provider` |
| `GCP_SERVICE_ACCOUNT` | `github-actions-deployer@my-project.iam.gserviceaccount.com` | Terraform output `deployer_service_account_email` |

## GitHub Secrets

Cloud Run 배포 인증에는 GitHub Secret이 필요하지 않다. 배포 workflow는 GitHub OIDC 토큰을 Google Workload Identity Federation에 교환해 짧은 수명의 GCP credential을 얻는다.

기존 `GCP_SA_KEY` 같은 서비스 계정 JSON key secret은 새 배포 흐름에서 사용하지 않는다. 장기 키가 남아 있다면 배포 전환 확인 후 폐기한다.

## Terraform

Terraform은 앱 배포 실행 도구가 아니다. 다음 GCP 기준 상태를 관리한다.

- Artifact Registry
- Cloud Run baseline
- 배포용 service account
- runtime service account
- GitHub OIDC Workload Identity Pool/Provider
- GitHub Actions impersonation IAM
- IAM
- Secret Manager
- DB password secret env 연결
- Cloud Run secret mount/env 연결

Cloud Run image revision은 GitHub Actions가 관리한다. Terraform은 `template[0].containers[0].image` drift를 되돌리지 않도록 구성되어 있다.

## Firebase Credential

Firebase는 CI/CD 도구가 아니라 백엔드 런타임 의존성이다. 현재 백엔드는 Firebase ID Token 검증과 Firebase Storage 업로드/삭제에 Firebase Admin SDK를 사용한다.

Cloud Run runtime 계약:

- `FIREBASE_CREDENTIALS_PATH=/secrets/firebase-adminsdk.json`
- `FIREBASE_STORAGE_BUCKET=<Firebase Storage bucket>`
- Secret Manager secret이 `/secrets/firebase-adminsdk.json`으로 mount됨

로컬 개발에서는 기존 classpath JSON fallback도 유지된다.

## Database Credential

DB는 Oracle Cloud에 올라간 MySQL을 사용한다. 애플리케이션 이미지는 비밀번호 없이 다음 환경변수 계약으로 실행된다.

- `DB_URL=jdbc:mysql://161.33.46.41:3306/aim?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8`
- `DB_USER=aim_be`
- `DB_PASSWORD=<Secret Manager latest version>`
- `DDL_AUTO=update`

DB 비밀번호는 저장소와 Terraform 변수 파일에 넣지 않고 Secret Manager secret version으로만 등록한다.

## 최초 설정 순서

1. Terraform 변수 값을 준비한다.
2. 기존 Cloud Run 서비스가 있다면 Terraform import를 먼저 수행한다.
3. `terraform init`, `terraform fmt -check`, `terraform validate`, `terraform plan`을 실행한다.
4. 의도한 변경만 있는지 확인한 뒤 `terraform apply`를 실행한다.
5. Firebase Admin SDK JSON을 Secret Manager secret version으로 등록한다.
6. DB password를 Secret Manager secret version으로 등록한다.
7. Terraform output `github_workload_identity_provider`를 GitHub Variable `GCP_WORKLOAD_IDENTITY_PROVIDER`에 등록한다.
8. Terraform output `deployer_service_account_email`을 GitHub Variable `GCP_SERVICE_ACCOUNT`에 등록한다.
9. 나머지 GitHub Variables를 등록한다.
10. 기존 `GCP_SA_KEY` secret을 쓰고 있었다면 새 workflow 배포 성공 후 폐기한다.
11. `feature/*` 브랜치에서 `main`으로 PR을 열어 CI check를 확인한다.
12. `main` merge 후 Cloud Run 배포 workflow를 확인한다.

## 실패 대응

- PR CI 실패: PR 하단 Checks에서 실패 job을 열고 Gradle compile/test 오류를 확인한다.
- deploy `verify` 실패: image build/push/deploy는 실행되지 않는다.
- GCP 인증 실패: `GCP_WORKLOAD_IDENTITY_PROVIDER`, `GCP_SERVICE_ACCOUNT`, Terraform WIF provider의 repository/ref 조건을 확인한다.
- Docker push 실패: Artifact Registry repository 이름, region, service account 권한을 확인한다.
- Cloud Run deploy 실패: deployer service account의 `roles/run.admin`, runtime service account에 대한 `roles/iam.serviceAccountUser`, Artifact Registry reader 권한을 확인한다.
- DB 연결 실패: `DB_PASSWORD` secret version 존재 여부, Cloud Run runtime service account의 secret accessor 권한, `DB_URL`/`DB_USER` 값을 확인한다.
- Firebase 초기화 실패: Secret Manager secret version 존재 여부, runtime service account의 secret accessor 권한, `FIREBASE_CREDENTIALS_PATH` mount를 확인한다.
