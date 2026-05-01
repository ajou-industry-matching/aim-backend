# AIM Backend Google Cloud Infrastructure

이 Terraform 모듈은 `aim-backend`의 Cloud Run 배포 기반을 관리한다. 애플리케이션 배포 자체는 GitHub Actions가 commit SHA 이미지로 수행하고, Terraform은 GCP 리소스와 IAM, Secret Manager 연결의 기준 상태만 관리한다.

## 관리 대상

- Artifact Registry Docker repository
- GitHub Actions 배포용 서비스 계정
- Cloud Run runtime 서비스 계정
- Cloud Run v2 서비스 baseline
- Firebase Admin SDK credential용 Secret Manager secret
- Artifact Registry, Cloud Run, Secret Manager IAM

## 이미지 소유권

Cloud Run 서비스에는 최초 생성용 `bootstrap_image`가 필요하다. 이후 컨테이너 image revision은 GitHub Actions가 소유한다.

`google_cloud_run_v2_service.app`는 `template[0].containers[0].image`를 `ignore_changes`로 둔다. 따라서 GitHub Actions가 배포한 최신 image가 다음 `terraform apply`에서 이전 bootstrap image로 되돌아가지 않는다.

## 서비스 계정 JSON 키

Terraform은 배포용 서비스 계정과 권한만 만든다. 서비스 계정 JSON key는 Terraform state에 private key가 남지 않도록 Terraform으로 만들지 않는다.

서비스 계정 생성 후 다음처럼 키를 만들고 GitHub Secret `GCP_SA_KEY`에 등록한다.

```bash
gcloud iam service-accounts keys create gcp-sa-key.json \
  --iam-account "$(terraform output -raw deployer_service_account_email)" \
  --project "<PROJECT_ID>"
```

키 파일 내용 전체를 GitHub repository secret `GCP_SA_KEY`로 저장한 뒤 로컬 파일은 삭제한다.

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
