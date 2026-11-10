# AIM Backend Google Cloud Infrastructure

이 Terraform 모듈은 `aim-backend`의 Cloud Run 배포 기반을 관리한다. 애플리케이션 배포 자체는 GitHub Actions가 commit SHA 이미지로 수행하고, Terraform은 GCP 리소스와 IAM, Secret Manager 연결의 기준 상태만 관리한다.

## 관리 대상

- Artifact Registry Docker repository
- GitHub Actions 배포용 서비스 계정
- Cloud Run runtime 서비스 계정
- GitHub OIDC Workload Identity Pool/Provider
- Cloud Run v2 서비스 baseline
- Firebase Admin SDK credential용 Secret Manager secret
- DB password용 Secret Manager secret
- Artifact Registry, Cloud Run, Secret Manager IAM

## 이미지 소유권

Cloud Run 서비스에는 최초 생성용 `bootstrap_image`가 필요하다. 이후 컨테이너 image revision은 GitHub Actions가 소유한다.

`google_cloud_run_v2_service.app`는 `template[0].containers[0].image`를 `ignore_changes`로 둔다. 따라서 GitHub Actions가 배포한 최신 image가 다음 `terraform apply`에서 이전 bootstrap image로 되돌아가지 않는다.

## GitHub Actions Workload Identity Federation

Terraform은 GitHub Actions가 장기 서비스 계정 JSON key 없이 배포할 수 있도록 Workload Identity Pool, OIDC Provider, 배포용 서비스 계정 impersonation 권한을 함께 만든다.

GitHub Actions는 `main` 브랜치의 `ajou-industry-matching/aim-backend` 실행에서만 배포용 서비스 계정을 impersonate할 수 있다. 이 제한은 `github_repository`, `github_deploy_ref` 변수로 조정할 수 있다.

`terraform apply` 후 GitHub Actions Variables에 다음 값을 등록한다.

| 이름 | Terraform output |
| --- | --- |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | `github_workload_identity_provider` |
| `GCP_SERVICE_ACCOUNT` | `deployer_service_account_email` |

```bash
gh variable set GCP_WORKLOAD_IDENTITY_PROVIDER \
  --body "$(terraform output -raw github_workload_identity_provider)"

gh variable set GCP_SERVICE_ACCOUNT \
  --body "$(terraform output -raw deployer_service_account_email)"
```

이 방식은 GitHub에 GCP private key를 저장하지 않고, workflow 실행 시점의 짧은 OIDC 토큰으로만 배포 권한을 얻는다.

## Secret

민감값 4개는 Secret Manager에 두고 Cloud Run env로 주입한다. Terraform은 secret container와 env 연결만 선언하고, **값(version)은 넣지 않는다.**

| secret | env | 값을 넣는 주체 |
| --- | --- | --- |
| `DB_URL` | `DB_URL` | 배포 워크플로 |
| `DB_PASSWORD` | `DB_PASSWORD` | 배포 워크플로 |
| `FIREBASE_CREDENTIALS_JSON` | `FIREBASE_CREDENTIALS_JSON` (base64 JSON) | 배포 워크플로 |
| `CRAWLER_API_KEY` | `CRAWLER_API_KEY` | 배포 워크플로 |

이름은 `secrets.tf`에 리터럴로 고정했다. 배포 워크플로(`.github/workflows/deploy-cloud-run.yml`)가 GitHub Secrets를 원본으로 같은 이름의 secret을 생성·갱신하고 같은 이름으로 주입하므로, 한쪽만 바꾸면 서비스가 다른 secret을 읽는다.

### 새 프로젝트에서 처음 apply 할 때

`version = "latest"`를 읽으므로 **version이 하나도 없는 secret을 참조하면 서비스 생성이 실패한다.** Terraform은 container만 만들기 때문에, 신규 프로젝트에서는 apply 전에 값을 한 번 넣는다.

```bash
for name in DB_URL DB_PASSWORD FIREBASE_CREDENTIALS_JSON CRAWLER_API_KEY; do
  gcloud secrets create "$name" --replication-policy=automatic --project "<PROJECT_ID>"
  printf %s '<VALUE>' | gcloud secrets versions add "$name" --data-file=- --project "<PROJECT_ID>"
done
```

`FIREBASE_CREDENTIALS_JSON`에 넣는 값은 **base64로 인코딩한** 서비스 계정 JSON이다. 앱이 `FirebaseAdminConfig`에서 base64로 디코딩한다.

이렇게 만든 secret은 Terraform 밖에서 생긴 것이므로, **apply 전에 import까지 해야 한다.** 그냥 apply하면 같은 이름으로 또 만들려다 실패한다. 기존 프로젝트도 워크플로가 이미 4개를 만들어 뒀으므로 마찬가지다. import 명령은 아래 "실행 예시"에 있다.

## Cloud Run env 계약

- secret 주입: `DB_URL`, `DB_PASSWORD`, `FIREBASE_CREDENTIALS_JSON`, `CRAWLER_API_KEY`
- 평문 주입: `SPRING_PROFILES_ACTIVE=prod`, `DB_USER=aim_be`, `DDL_AUTO=validate`, `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`, `FIREBASE_STORAGE_BUCKET=<bucket>`
- Flyway: `SPRING_FLYWAY_ENABLED=true`, `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true`, `SPRING_FLYWAY_BASELINE_VERSION=1`

`DDL_AUTO`와 `SPRING_JPA_HIBERNATE_DDL_AUTO`는 운영 DB 스키마 자동 삭제/생성을 막기 위해 `validate` 또는 `none`만 허용한다.

운영 설정은 `application-prod.yml`이 아니라 위 env로 넘긴다. 이미지 빌드에서 프로파일 yml이 제외돼 설정이 조용히 사라진 적이 있다(#59).

## 실행 예시

```bash
terraform init
terraform fmt -check
terraform validate
terraform plan \
  -var="project_id=<PROJECT_ID>" \
  -var="cloud_run_region=asia-northeast3" \
  -var="artifact_registry_region=asia-northeast3" \
  -var="service_name=aim-be-prod" \
  -var="bootstrap_image=<ARTIFACT_REGISTRY_REGION>-docker.pkg.dev/<PROJECT_ID>/<REPOSITORY>/aim-backend:bootstrap"
```

`asia-northeast3`에 `aim-be-prod` service가 이미 생성되어 있다면(예: 배포 workflow가 먼저 생성한 경우) 새로 만들기 전에 import를 먼저 한다. 이 import는 `asia-northeast3`에 이미 존재하는 service에만 적용되며, 기존 `us-central1` service를 옮기지는 않는다. 새 service를 Terraform으로 직접 생성할 때는 import 없이 `terraform apply`로 만든다. 기존 `us-central1` service는 cleanup 전까지 별도 service로 유지된다.

```bash
terraform import \
  'google_cloud_run_v2_service.app' \
  'projects/<PROJECT_ID>/locations/asia-northeast3/services/aim-be-prod'
```

secret 4개도 워크플로가 이미 만들어 둔 것이므로 함께 import한다. 안 하면 apply가 같은 이름으로 새로 만들려다 실패한다.

```bash
terraform import 'google_secret_manager_secret.db_url'               'projects/<PROJECT_ID>/secrets/DB_URL'
terraform import 'google_secret_manager_secret.db_password'          'projects/<PROJECT_ID>/secrets/DB_PASSWORD'
terraform import 'google_secret_manager_secret.firebase_credentials' 'projects/<PROJECT_ID>/secrets/FIREBASE_CREDENTIALS_JSON'
terraform import 'google_secret_manager_secret.crawler_api_key'      'projects/<PROJECT_ID>/secrets/CRAWLER_API_KEY'
```

import 후 `terraform plan`에서 의도하지 않은 service account, secret, env, ingress 변경이 없는지 확인한다. state가 원격 백엔드 없이 로컬에만 있으므로, apply는 state를 가진 한 사람이 배포 워크플로와 겹치지 않는 시점에 수행한다.
