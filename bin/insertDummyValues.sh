#!/usr/bin/env bash


command cat BANKaccount_data.sql | docker exec -i db psql -U postgres -d BANK
command cat BANK2account_data.sql | docker exec -i dbTwo psql -U postgres -d BANK2
#command cat ./bank2/data.sql | docker exec -i db psql -U postgres -d BANK


exit 0
#docker rm -f $(docker ps -a -q)        <-- remove all closed containers
#docker volume rm $(docker volume ls -q)  <-- remove all volumes