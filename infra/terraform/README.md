# AIM Backend Google Cloud Infrastructure

이 Terraform 모듈은 `aim-backend`의 Cloud Run 배포 기반을 관리한다. 애플리케이션 배포 자체는 GitHub Actions가 commit SHA 이미지로 수행하고, Terraform은 GCP 리소스와 IAM, Secret Manager 연결의 기준 상태만 관리한다.

## 관리 대상

- Artifact Registry Docker repository
- GitHub Actions 배포용 서비스 계정
- Cloud Run runtime 서비스 계정
- GitHub OIDC Workload Identity Pool/Provider
- Cloud Run v2 서비스 baseline
- Firebase Admin SDK credential용 Secret Manager secret
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

## Firebase Secret

Terraform은 secret container만 만든다. 실제 Firebase Admin SDK JSON payload는 저장소나 Terraform 변수에 넣지 않고 별도로 등록한다.

```bash
gcloud secrets versions add "$(terraform output -raw firebase_credentials_secret_id)" \
  --data-file="src/main/resources/firebase/ajou-project-cafd9-firebase-adminsdk-fbsvc-e6d8a32d57.json" \
  --project "<PROJECT_ID>"
```

Cloud Run에는 다음 계약으로 mount된다.

- mount path: `/secrets/firebase-adminsdk.json`
- env: `FIREBASE_CREDENTIALS_PATH=/secrets/firebase-adminsdk.json`
- env: `FIREBASE_STORAGE_BUCKET=<bucket>`

## 실행 예시

```bash
terraform init
terraform fmt -check
terraform validate
terraform plan \
  -var="project_id=<PROJECT_ID>" \
  -var="bootstrap_image=<REGION>-docker.pkg.dev/<PROJECT_ID>/<REPOSITORY>/aim-backend:bootstrap"
```

기존 Cloud Run 서비스가 이미 있다면 새로 만들기 전에 import를 먼저 한다.

```bash
terraform import \
  'google_cloud_run_v2_service.app' \
  'projects/<PROJECT_ID>/locations/<REGION>/services/<SERVICE_NAME>'
```

import 후 `terraform plan`에서 의도하지 않은 service account, secret, env, ingress 변경이 없는지 확인한다.
