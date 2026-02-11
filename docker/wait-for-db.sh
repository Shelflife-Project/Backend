#!/usr/bin/env bash
set -euo pipefail

DB_HOST="${DB_HOST:-db}"
DB_PORT="${DB_PORT:-3306}"
DB_WAIT_TIMEOUT="${DB_WAIT_TIMEOUT:-60}"

echo "Waiting for database $DB_HOST:$DB_PORT (timeout=${DB_WAIT_TIMEOUT}s)"
start_time=$(date +%s)
while true; do
  if bash -c "</dev/tcp/${DB_HOST}/${DB_PORT}" >/dev/null 2>&1; then
    echo "$DB_HOST:$DB_PORT is available"
    break
  fi
  now=$(date +%s)
  if [ $((now - start_time)) -ge "$DB_WAIT_TIMEOUT" ]; then
    echo "Timed out waiting for ${DB_HOST}:${DB_PORT} after ${DB_WAIT_TIMEOUT}s" >&2
    exit 1
  fi
  sleep 1
done

exec java -jar /app/app.jar
