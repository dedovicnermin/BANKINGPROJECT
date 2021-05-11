#!/usr/bin/env bash

echo "create topic postgres.connector.bank.accounts"
echo "========================================="
docker exec -t zookeeper kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1 --topic postgres.bank.accounts


echo "create topic postgres.connector.bank.transactions"
echo "========================================="
docker exec -t zookeeper kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1 --topic postgres.bank.transactions


#//"topic.prefix": "postgres.bank.",