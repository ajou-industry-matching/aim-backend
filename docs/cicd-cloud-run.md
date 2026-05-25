# GitHub Actions -> Cloud Run CI/CD 운영 문서

## 브랜치 전략

- 기본 브랜치: `main`
- 개발 브랜치: `feature/*`
- 병합 흐름: `feature/*`에서 `main`으로 Pull Request 생성
- PR 하단 Checks 영역에 `CI / Gradle test and build` 결과가 표시된다.
- `main`에 merge되면 `CI`와 `Deploy to Cloud Run` workflow가 각각 실행된다.

## GitHub Actions

### CI

파일: `.github/workflows/ci.yml`

`pull_request`가 `main`을 대상으로 열리거나 갱신될 때, 그리고 `main` push가 발생할 때 실행된다.

검증 내용:

- Java 17 설정
- Gradle cache 설정
- `./gradlew test bootJar --no-daemon`
- GitHub Actions workflow 문법 검증 (`actionlint`)
- PR용 더미 런타임 환경변수 계약 검증 (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `DDL_AUTO`, `FIREBASE_STORAGE_BUCKET`)
- Docker image build
- MySQL 컨테이너와 함께 애플리케이션 컨테이너를 실행한 뒤 `/api/health` smoke test

현재 저장소에는 별도 테스트 코드가 없으므로, 이 단계는 컴파일과 Gradle test task 성공 여부를 먼저 보장한다. 추가로 Docker 이미지가 실제 컨테이너로 기동되고 `PORT=8080`에서 health check에 응답하는지 검증해 Cloud Run deploy 단계에서 발견되던 startup 실패를 PR 단계에서 최대한 앞당겨 잡는다.

CI smoke test는 운영 DB나 Firebase 실계정을 사용하지 않는다. CI 안에서 MySQL 컨테이너를 임시로 띄우고, Firebase Admin SDK 초기화는 `FIREBASE_ENABLED=false`로 비활성화한다. GCP Workload Identity Federation, Cloud Run IAM, Artifact Registry push 권한처럼 실제 GCP 리소스 권한이 필요한 영역은 `Deploy to Cloud Run` workflow에서 검증한다.

### Deploy

파일: `.github/workflows/deploy-cloud-run.yml`

`main` push 또는 수동 `workflow_dispatch`에서 실행된다.

job 구조:

- `deploy`: 배포 설정 검증, GCP 인증, Docker image build/push, Cloud Run deploy 실행

배포 내용:

- GitHub OIDC와 Google Workload Identity Federation으로 GCP 인증
- Artifact Registry Docker auth 설정
- Docker Buildx cache 기반 Docker image build
- Artifact Registry push
- Cloud Run revision 배포

`Deploy to Cloud Run` workflow는 더 이상 Gradle test/build 검증을 별도 job으로 반복하지 않는다. 코드 검증, workflow lint, Docker smoke test는 `CI` workflow가 담당하고, 배포 workflow는 Cloud Run 배포에 필요한 런타임 설정과 GCP 권한 영역에 집중한다. 현재 구조에서는 `main` push 시 `CI`와 `Deploy to Cloud Run`이 독립적으로 실행된다. 배포를 CI 성공 이후로 강제해야 한다면 branch protection 또는 `workflow_run` 기반 순차 실행을 별도 이슈로 검토한다.

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

운영 DB는 MySQL을 사용한다. 애플리케이션 이미지는 비밀번호 없이 다음 환경변수 계약으로 실행된다.

- `DB_URL=<JDBC URL>`
- `DB_USER=<DB username>`
- `DB_PASSWORD=<GitHub Actions Secret 또는 Secret Manager latest version>`
- `SPRING_PROFILES_ACTIVE=prod`
- `DDL_AUTO=validate`

DB 접속 정보와 비밀번호는 저장소와 Terraform 변수 파일에 넣지 않는다. 현재 GitHub Actions 배포 workflow는 `DB_URL`, `DB_PASSWORD`를 GitHub Actions Secret으로 받고, `DB_USER`, `DDL_AUTO`를 GitHub Actions Variable로 받아 Cloud Run revision 환경변수에 주입한다. 운영 환경에서는 Hibernate가 스키마를 자동 생성하거나 삭제하지 않도록 `DDL_AUTO` 기본값을 `validate`로 두고, Cloud Run 배포에서는 `validate` 또는 `none`만 허용한다.

## Image Build Optimization

Dockerfile은 로컬 빌드가 가능한 멀티 스테이지 구조를 유지한다. 기본 `runtime` target은 Docker build 내부에서 Gradle `bootJar`를 실행하고, `runtime-prebuilt` target은 이미 생성된 `build/libs/aim-be.jar`를 이미지에 복사한다.

CI workflow의 이미지 검증 계약:

- `./gradlew test bootJar --no-daemon` 실행
- 배포 workflow와 같은 `runtime` target으로 Docker image build
- CI 전용 MySQL 컨테이너와 함께 애플리케이션 컨테이너 smoke test

배포 workflow의 이미지 빌드 계약:

- 별도 Gradle verify job 없이 Docker Buildx로 `runtime` target build/push
- Docker Buildx `type=gha` cache로 레이어 캐시 재사용
- 최종 런타임 이미지는 non-root distroless Java 17 기반으로 실행

## 최초 설정 순서

1. Terraform 변수 값을 준비한다.
2. 기존 Cloud Run 서비스가 있다면 Terraform import를 먼저 수행한다.
3. `terraform init`, `terraform fmt -check`, `terraform validate`, `terraform plan`을 실행한다.
4. 의도한 변경만 있는지 확인한 뒤 `terraform apply`를 실행한다.
5. Firebase Admin SDK JSON을 Secret Manager secret version으로 등록한다.
6. DB 접속 정보와 DB password를 GitHub Actions Secret/Variable 또는 Secret Manager secret version으로 등록한다.
7. Terraform output `github_workload_identity_provider`를 GitHub Variable `GCP_WORKLOAD_IDENTITY_PROVIDER`에 등록한다.
8. Terraform output `deployer_service_account_email`을 GitHub Variable `GCP_SERVICE_ACCOUNT`에 등록한다.
9. 나머지 GitHub Variables를 등록한다.
10. 기존 `GCP_SA_KEY` secret을 쓰고 있었다면 새 workflow 배포 성공 후 폐기한다.
11. `feature/*` 브랜치에서 `main`으로 PR을 열어 CI check를 확인한다.
12. `main` merge 후 Cloud Run 배포 workflow를 확인한다.

## 실패 대응

- PR CI 실패: PR 하단 Checks에서 실패 job을 열고 Gradle compile/test 오류를 확인한다.
- deploy 설정 검증 실패: 필수 GitHub Variables/Secrets 값을 확인한다.
- GCP 인증 실패: `GCP_WORKLOAD_IDENTITY_PROVIDER`, `GCP_SERVICE_ACCOUNT`, Terraform WIF provider의 repository/ref 조건을 확인한다.
- Docker push 실패: Artifact Registry repository 이름, region, service account 권한을 확인한다.
- Cloud Run deploy 실패: deployer service account의 `roles/run.admin`, runtime service account에 대한 `roles/iam.serviceAccountUser`, Artifact Registry reader 권한을 확인한다.
- DB 연결 실패: `DB_PASSWORD` secret version 존재 여부, Cloud Run runtime service account의 secret accessor 권한, `DB_URL`/`DB_USER` 값을 확인한다.
- Firebase 초기화 실패: Secret Manager secret version 존재 여부, runtime service account의 secret accessor 권한, `FIREBASE_CREDENTIALS_PATH` mount를 확인한다.
