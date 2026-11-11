#!/bin/sh
set -eu

# Runs only when the PostgreSQL data directory is first initialized.
psql --set ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
\getenv migrator_password PACT_DB_MIGRATOR_PASSWORD
\getenv app_password PACT_DB_APP_PASSWORD
CREATE ROLE pact_migrator LOGIN PASSWORD :'migrator_password';
CREATE ROLE pact_app LOGIN PASSWORD :'app_password';
REVOKE CONNECT ON DATABASE pact FROM PUBLIC;
GRANT CONNECT, CREATE ON DATABASE pact TO pact_migrator;
GRANT CONNECT ON DATABASE pact TO pact_app;
SQL
