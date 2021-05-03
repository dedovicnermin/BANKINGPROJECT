#!/usr/bin/env bash


command cat BANKaccount_data.sql | docker exec -i db psql -U postgres -d BANK
command cat BANK2account_data.sql | docker exec -i dbTwo psql -U postgres -d BANK2



#docker rm -f $(docker ps -a -q)
#docker volume rm $(docker volume ls -q)