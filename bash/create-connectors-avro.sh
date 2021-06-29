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



printf "\n=== Register connect schemas ===\n"



printf "\nRegister postgres.transactions-value \n"

echo
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema":"{\"type\":\"record\",\"name\":\"Transaction\",\"namespace\":\"tech.nermindedovic.persistence.data.entity\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"transaction_id\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"amount\",\"type\":[\"null\",\"double\"],\"default\":null},{\"name\":\"creditor_account\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"date\",\"type\":[\"null\",{\"type\":\"long\",\"connect.version\":1,\"connect.name\":\"org.apache.kafka.connect.data.Timestamp\",\"logicalType\":\"timestamp-millis\"}],\"default\":null},{\"name\":\"debtor_account\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"memo\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"origin\",\"type\":[\"null\",\"string\"],\"default\":null}],\"connect.name\":\"tech.nermindedovic.persistence.data.entity.Transaction\"}"}' \
	http://localhost:8081/subjects/postgres.transactions-value/versions


printf "\nRegister postgres2.transactions-value \n"

echo
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema":"{\"type\":\"record\",\"name\":\"Transaction\",\"namespace\":\"tech.nermindedovic.persistence.data.entity\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"transaction_id\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"amount\",\"type\":[\"null\",\"double\"],\"default\":null},{\"name\":\"creditor_account\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"date\",\"type\":[\"null\",{\"type\":\"long\",\"connect.version\":1,\"connect.name\":\"org.apache.kafka.connect.data.Timestamp\",\"logicalType\":\"timestamp-millis\"}],\"default\":null},{\"name\":\"debtor_account\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"memo\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"origin\",\"type\":[\"null\",\"string\"],\"default\":null}],\"connect.name\":\"tech.nermindedovic.persistence.data.entity.Transaction\"}"}' \
	http://localhost:8081/subjects/postgres2.transactions-value/versions


printf "\nRegister postgres.transactions-key \n"

echo
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema": "{\"type\": \"int\"}"}' \
  http://localhost:8081/subjects/postgres.transactions-key/versions





printf "\nRegister postgres2.transactions-key \n"

echo
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema": "{\"type\": \"int\"}"}' \
  http://localhost:8081/subjects/postgres2.transactions-key/versions



printf "\nExecute KSQL statements\n"

echo
curl -X POST http://localhost:8088/ksql \
     -H "Accept: application/vnd.ksql.v1+json" \
     -d $'{
  "ksql": "CREATE STREAM errors (errorMsg STRING) WITH (KAFKA_TOPIC = \'funds.transfer.error\', KEY_FORMAT=\'NONE\', VALUE_FORMAT=\'DELIMITED\'); CREATE STREAM ERROR_COUNT WITH (KAFKA_TOPIC=\'error_count\', PARTITIONS=1, REPLICAS=1) AS SELECT   1 KEYCOL,   ERRORS.ERRORMSG ERRORMSG FROM ERRORS ERRORS; CREATE TABLE error_count_table AS SELECT KEYCOL, count(KEYCOL) AS COUNT FROM error_count GROUP BY KEYCOL; CREATE STREAM b1 WITH (KAFKA_TOPIC = \'postgres.transactions\', VALUE_FORMAT = \'AVRO\'); CREATE STREAM B1_REKEYED AS SELECT TRANSACTION_ID, AMOUNT, CREDITOR_ACCOUNT, DEBTOR_ACCOUNT, DATE, ORIGIN FROM B1 PARTITION BY TRANSACTION_ID; CREATE TABLE B1_TABLE WITH (VALUE_FORMAT=\'AVRO\') AS SELECT \'BANK1\' AS BANK, ORIGIN, COUNT(*) AS COUNT FROM B1_REKEYED GROUP BY ORIGIN; CREATE STREAM b2 WITH (KAFKA_TOPIC = \'postgres2.transactions\', VALUE_FORMAT = \'AVRO\'); CREATE STREAM B2_REKEYED AS SELECT TRANSACTION_ID, AMOUNT, CREDITOR_ACCOUNT, DEBTOR_ACCOUNT, DATE, ORIGIN FROM B2 PARTITION BY TRANSACTION_ID; CREATE TABLE B2_TABLE WITH (VALUE_FORMAT=\'AVRO\') AS SELECT \'BANK2\' AS BANK, ORIGIN, COUNT(*) AS COUNT FROM B2_REKEYED GROUP BY ORIGIN;"
}'

printf "\n\n=== DONE ===\n"


printf "\n========"
echo "Done!"
printf "========\n"



