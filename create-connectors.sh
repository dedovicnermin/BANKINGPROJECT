#!/usr/bin/env bash

echo "====================="
echo "Creating connectors! "
echo "======================"


printf "\n===== BANK1 config - accounts =====\n"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @bin/bank/jdbc-source-accounts.json

printf "\n===== Completed (1/4) =====\n"



printf "\n===== BANK1 config - transactions =====\n"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @bin/bank/jdbc-source-transactions.json

printf "\n===== Completed (2/4) =====\n"


#########################################################


printf "\n===== BANK2 config - accounts =====\n"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @bin/bank2/jdbc-source-accounts.json

printf "\n===== Completed (3/4) =====\n"



printf "\n===== BANK2 config - transactions =====\n"

curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @bin/bank2/jdbc-source-transactions.json

printf "\n===== Completed (4/4) =====\n"


printf "\n========"
echo "Done!"
printf "========\n"
