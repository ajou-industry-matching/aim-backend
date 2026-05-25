#!/usr/bin/env bash
set -euo pipefail

required_vars=(
  DB_URL
  DB_USER
  DB_PASSWORD
  DDL_AUTO
  FIREBASE_STORAGE_BUCKET
)

missing=0

for name in "${required_vars[@]}"; do
  if [ -z "${!name:-}" ]; then
    echo "::error::${name} is required"
    missing=1
  fi
done

if [ -n "${DDL_AUTO:-}" ]; then
  case "${DDL_AUTO}" in
    validate|none)
      ;;
    *)
      echo "::error::DDL_AUTO must be validate or none for Cloud Run deployments"
      missing=1
      ;;
  esac
fi

exit "${missing}"
