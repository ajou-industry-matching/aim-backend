#!/usr/bin/env bash
set -euo pipefail

required_vars=(
  GCP_PROJECT_ID
  GCP_REGION
  CLOUD_RUN_SERVICE
  ARTIFACT_REGISTRY_REPOSITORY
  IMAGE_NAME
  GCP_WORKLOAD_IDENTITY_PROVIDER
  GCP_SERVICE_ACCOUNT
  SPRING_PROFILES_ACTIVE
)

missing=0

for name in "${required_vars[@]}"; do
  if [ -z "${!name:-}" ]; then
    echo "::error::${name} is required"
    missing=1
  fi
done

if [ -n "${SPRING_PROFILES_ACTIVE:-}" ] && [ "${SPRING_PROFILES_ACTIVE}" != "prod" ]; then
  echo "::error::SPRING_PROFILES_ACTIVE must be prod for Cloud Run deployments"
  missing=1
fi

bash .github/scripts/validate-runtime-env.sh || missing=1

exit "${missing}"
