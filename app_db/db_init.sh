#!/bin/bash

set -e
set -u

function create_dbs_and_users() {
  local database=$1
  echo "create database: '$database' with user: '$database'"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
      CREATE USER "$database" WITH PASSWORD '$database';
      CREATE DATABASE "$database";
EOSQL

  # Now, apply grants ONLY within the newly created database
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$database" <<-EOSQL
      GRANT ALL PRIVILEGES ON DATABASE "$database" TO "$database";
      GRANT USAGE, CREATE ON SCHEMA public TO "$database";
      ALTER SCHEMA public OWNER TO "$database";
      GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "$database";
      GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "$database";
EOSQL
}

if [ -n "$db_names" ]
then
  separated_db_names=$(echo "$db_names" | tr ',' ' ')
  for database in $separated_db_names
  do
    create_dbs_and_users "$database"
  done
else
  echo "'db_names' was not set. No databases created."
fi