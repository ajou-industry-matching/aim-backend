#!/usr/bin/env bash
set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-aim-backend-ci}"
NETWORK_NAME="${NETWORK_NAME:-aim-ci-smoke}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-aim-ci-mysql}"
APP_CONTAINER="${APP_CONTAINER:-aim-ci-app}"
PORT="${PORT:-8080}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-root_password}"

cleanup() {
  status=$?

  if [ "${status}" -ne 0 ]; then
    docker logs "${MYSQL_CONTAINER}" 2>/dev/null || true
    docker logs "${APP_CONTAINER}" 2>/dev/null || true
  fi

  docker rm -f "${APP_CONTAINER}" "${MYSQL_CONTAINER}" >/dev/null 2>&1 || true
  docker network rm "${NETWORK_NAME}" >/dev/null 2>&1 || true
}

trap cleanup EXIT

docker rm -f "${APP_CONTAINER}" "${MYSQL_CONTAINER}" >/dev/null 2>&1 || true
docker network rm "${NETWORK_NAME}" >/dev/null 2>&1 || true

docker network create "${NETWORK_NAME}" >/dev/null

docker run \
  --detach \
  --name "${MYSQL_CONTAINER}" \
  --network "${NETWORK_NAME}" \
  --env MYSQL_DATABASE=aim \
  --env MYSQL_USER="${DB_USER}" \
  --env MYSQL_PASSWORD="${DB_PASSWORD}" \
  --env MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
  mysql:8.4 >/dev/null

for attempt in $(seq 1 60); do
  if docker exec "${MYSQL_CONTAINER}" mysqladmin ping -h 127.0.0.1 -uroot "-p${MYSQL_ROOT_PASSWORD}" --silent; then
    break
  fi

  if [ "${attempt}" -eq 60 ]; then
    echo "::error::MySQL container did not become ready"
    exit 1
  fi

  sleep 2
done

docker run \
  --detach \
  --name "${APP_CONTAINER}" \
  --network "${NETWORK_NAME}" \
  --publish "${PORT}:${PORT}" \
  --env PORT="${PORT}" \
  --env DB_URL="${DB_URL}" \
  --env DB_USER="${DB_USER}" \
  --env DB_PASSWORD="${DB_PASSWORD}" \
  --env DDL_AUTO="${DDL_AUTO}" \
  --env SPRING_JPA_HIBERNATE_DDL_AUTO="${SPRING_JPA_HIBERNATE_DDL_AUTO}" \
  --env FIREBASE_ENABLED=false \
  --env FIREBASE_STORAGE_BUCKET="${FIREBASE_STORAGE_BUCKET}" \
  "${IMAGE_NAME}" >/dev/null

for attempt in $(seq 1 60); do
  if response="$(curl -fsS "http://localhost:${PORT}/api/health" 2>/dev/null)" && [ "${response}" = "ok" ]; then
    echo "Docker smoke test passed"
    exit 0
  fi

  if ! docker ps --format '{{.Names}}' | grep -Fxq "${APP_CONTAINER}"; then
    echo "::error::Application container exited before health check passed"
    exit 1
  fi

  if [ "${attempt}" -eq 60 ]; then
    echo "::error::Application did not respond on /api/health"
    exit 1
  fi

  sleep 2
done
