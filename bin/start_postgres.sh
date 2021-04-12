#!/usr/bin/env bash


#pipefail - if set, the return value of array pipeline is the value of the last (rightmost) command to exit with with a non-zero status
set -euo pipefail
which psql > /dev/null || (echo "Please ensure that postgres client is in your PATH" && exit 1)

mkdir -p $HOME/docker/volumes/postgres
rm -rf $HOME/docker/volumes/postgres/data


docker run --rm --name docker-postgres-bank -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=BANK -d -p 5432:5432 -v $HOME/docker/volumes/postgres:/var/lib/postgresql postgres

sleep 3
export PGPASSWORD=postgres

psql -U postgres -d BANK -h localhost -f bank_schema.sql
psql -U postgres -d BANK -h localhost -f account_data.sql




