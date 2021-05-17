#!/usr/bin/env bash

echo "====================="
echo "Creating connectors! "
echo "======================"


printf "\n===== BANK1 config - accounts =====\n"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @bank/jdbc-source-accounts.json





printf "\n===== BANK1 config - transactions =====\n"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @bank/jdbc-source-transactions-avro.json




#########################################################


printf "\n===== BANK2 config - accounts =====\n"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @bank2/jdbc-source-accounts.json





printf "\n===== BANK2 config - transactions =====\n"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @bank2/jdbc-source-transactions-avro.json



printf "\n===== Elastic config - transactions =====\n"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @elastic/elastic-sink-banks-transaction-avro.json



printf "\n=== Kibana Import Starting  ===\n"

echo
curl -X POST "http://localhost:5601/api/saved_objects/_import" -H "kbn-xsrf: true" --form file=@kibana_config.ndjson

printf "\n\n=== DONE ===\n"


printf "\n========"
echo "Done!"
printf "========\n"



