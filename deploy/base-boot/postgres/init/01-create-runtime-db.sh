#!/bin/sh
set -eu

runtime_db="${POSTGRES_RUNTIME_DB:-ai_runtime}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
SELECT 'CREATE DATABASE "$runtime_db"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$runtime_db')\gexec
EOSQL
