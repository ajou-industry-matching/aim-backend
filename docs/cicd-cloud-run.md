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

- 서비스 계정 JSON key로 GCP 인증
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
| `CLOUD_RUN_SERVICE` | `aim-backend` | Cloud Run service name |
| `ARTIFACT_REGISTRY_REPOSITORY` | `aim` | Artifact Registry repository ID |
| `IMAGE_NAME` | `aim-backend` | Docker image name |

## GitHub Secrets

| 이름 | 설명 |
| --- | --- |
| `GCP_SA_KEY` | GitHub Actions 배포용 GCP service account JSON key 전체 내용 |

서비스 계정 JSON key는 장기 credential이다. repository file로 저장하지 말고 GitHub Secret으로만 등록한다. 키 파일을 로컬에서 만든 뒤 Secret 등록이 끝나면 삭제한다.

## Terraform

Terraform은 앱 배포 실행 도구가 아니다. 다음 GCP 기준 상태를 관리한다.

- Artifact Registry
- Cloud Run baseline
- 배포용 service account
- runtime service account
- IAM
- Secret Manager
- Cloud Run secret mount/env 연결

Cloud Run image revision은 GitHub Actions가 관리한다. Terraform은 `template[0].containers[0].image` drift를 되돌리지 않도록 구성되어 있다.

## Firebase Credential

Firebase는 CI/CD 도구가 아니라 백엔드 런타임 의존성이다. 현재 백엔드는 Firebase ID Token 검증과 Firebase Storage 업로드/삭제에 Firebase Admin SDK를 사용한다.

Cloud Run runtime 계약:

- `FIREBASE_CREDENTIALS_PATH=/secrets/firebase-adminsdk.json`
- `FIREBASE_STORAGE_BUCKET=<Firebase Storage bucket>`
- Secret Manager secret이 `/secrets/firebase-adminsdk.json`으로 mount됨

로컬 개발에서는 기존 classpath JSON fallback도 유지된다.

## 최초 설정 순서

1. Terraform 변수 값을 준비한다.
2. 기존 Cloud Run 서비스가 있다면 Terraform import를 먼저 수행한다.
3. `terraform init`, `terraform fmt -check`, `terraform validate`, `terraform plan`을 실행한다.
4. 의도한 변경만 있는지 확인한 뒤 `terraform apply`를 실행한다.
5. Firebase Admin SDK JSON을 Secret Manager secret version으로 등록한다.
6. 배포용 service account key를 생성한다.
7. key JSON 전체를 GitHub Secret `GCP_SA_KEY`에 등록한다.
8. GitHub Variables를 등록한다.
9. `feature/*` 브랜치에서 `main`으로 PR을 열어 CI check를 확인한다.
10. `main` merge 후 Cloud Run 배포 workflow를 확인한다.

## 실패 대응

- PR CI 실패: PR 하단 Checks에서 실패 job을 열고 Gradle compile/test 오류를 확인한다.
- deploy `verify` 실패: image build/push/deploy는 실행되지 않는다.
- Docker push 실패: Artifact Registry repository 이름, region, service account 권한을 확인한다.
- Cloud Run deploy 실패: deployer service account의 `roles/run.admin`, runtime service account에 대한 `roles/iam.serviceAccountUser`, Artifact Registry reader 권한을 확인한다.
- Firebase 초기화 실패: Secret Manager secret version 존재 여부, runtime service account의 secret accessor 권한, `FIREBASE_CREDENTIALS_PATH` mount를 확인한다.
