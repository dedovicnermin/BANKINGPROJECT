#!/usr/bin/env bash

#jq . transactions.json
#jq .transactions transactions.json
#jq '.transactions[1]' transactions.json
#jq '.transactions | length' transactions.json
#jq '.transactions[0] | length' transactions.json

# --- Grab len of transaction array ---
MYLEN=$(jq '.transactions | length' transactions.json)
echo "LENGTH: $MYLEN"


# --- Iterate over array, send post request to enter transaction ---
i=0
while [ $i -lt $MYLEN ]
do
	echo $i
	OBJ="$(jq .transactions[$i] transactions.json)"
	echo $OBJ
	curl -X POST --anyauth -u admin:admin --header "Content-Type:application/json" \
	-d "$OBJ" \
	http://localhost:8080/funds/transfer

	sleep .5
	let "i+=1"
done


exit 0


