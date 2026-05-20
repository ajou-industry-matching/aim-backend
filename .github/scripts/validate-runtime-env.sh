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

exit "${missing}"
