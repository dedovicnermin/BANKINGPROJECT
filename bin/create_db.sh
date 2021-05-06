#!/usr/bin/env bash

set -e

POSTGRES="psql --username ${POSTGRES_USER}"

echo "Creating database: ${POSTGRES_DB}"

$POSTGRES <<EOSQL
CREATE DATABASE ${DB_NAME} OWNER ${POSTGRES_USER};
EOSQL

echo "Creating schema..."
psql -d ${DB_NAME} -a -U${POSTGRES_USER} -f ./schema.sql

echo "Populating database..."
psql -d ${DB_NAME} -a  -U${POSTGRES_USER} -f ./${BANK_FILE}/data.sql