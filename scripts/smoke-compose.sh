#!/usr/bin/env bash
set -euo pipefail

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Required command is unavailable: $1" >&2
    exit 1
  }
}

for command in docker grep mktemp od tr; do
  require_command "$command"
done

project="pact-smoke-$(date +%s)-$$"
environment_file="$(mktemp)"
compose=(docker compose --project-name "$project" --env-file "$environment_file")
ui_port="${PACT_SMOKE_UI_PORT:-15606}"

cleanup() {
  "${compose[@]}" down --volumes --remove-orphans >/dev/null 2>&1 || true
  rm -f "$environment_file"
}
trap cleanup EXIT

password() {
  od -An -N24 -tx1 /dev/urandom | tr -d ' \n'
}

umask 077
printf 'PACT_DB_ADMIN_PASSWORD=%s\nPACT_DB_MIGRATOR_PASSWORD=%s\nPACT_DB_APP_PASSWORD=%s\nPACT_UI_PORT=%s\n' \
  "$(password)" "$(password)" "$(password)" "$ui_port" >"$environment_file"

wait_for_health() {
  local service="$1"
  local container
  container="$("${compose[@]}" ps --quiet "$service")"
  for _ in $(seq 1 60); do
    if [ "$(docker inspect --format '{{.State.Health.Status}}' "$container")" = "healthy" ]; then
      return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for $service to become healthy" >&2
  "${compose[@]}" logs >&2
  exit 1
}

"${compose[@]}" up --build --detach
wait_for_health postgres
wait_for_health backend
wait_for_health frontend

frontend_request() {
  "${compose[@]}" exec --no-TTY frontend wget --quiet --output-document=- "http://127.0.0.1:8080$1"
}

test "$("${compose[@]}" port frontend 8080)" = "127.0.0.1:$ui_port"
frontend_request / | grep --quiet 'id="app"'
frontend_request /api/system/info | grep --quiet '"name":"PACT"'
frontend_request /unknown/path | grep --quiet 'id="app"'

"${compose[@]}" exec --no-TTY postgres psql -U postgres -d pact -v ON_ERROR_STOP=1 \
  -c 'CREATE TABLE smoke_marker (id integer primary key); INSERT INTO smoke_marker VALUES (1);'
"${compose[@]}" down
docker volume inspect "${project}_postgres_data" >/dev/null
"${compose[@]}" up --detach
wait_for_health postgres
wait_for_health backend
wait_for_health frontend
"${compose[@]}" exec --no-TTY postgres psql -U postgres -d pact -tAc 'SELECT count(*) FROM smoke_marker' | grep --quiet '^1$'

echo "Compose smoke test passed."
