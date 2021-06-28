> ALL PASSWORDS == 123456
> alias for CA === CARoot
> export PASS=123456
> export CAA=CARoot
> export clientAlias=client
> export restAlias=rest
> export transformerAlias=transformer
> export routerAlias=router
> export persistenceAlias=persistence
> export persistence2Alias=persistence2
> export schemaAlias=schemaregistry
> export connectAlias=connect
> export ksqlAlias=ksql



i. set up CA:
-   openssl req -new -newkey rsa:4096 -days 365 -x509 -subj "/CN=Kafka-Security-CA" -keyout ca-key -out ca-cert -nodes
-   keytool -printcert -v -file ca-cert


ii. set up broker keystore/truststore
-   keytool -genkey -keystore kafka.server.keystore.jks -validity 365 -storepass $PASS -keypass $PASS -dname "CN=localhost" -storetype pkcs12
-   keytool -list -v -keystore kafka.server.keystore.jks

iii. request broker public key to be signed by the CA.
-   keytool -keystore kafka.server.keystore.jks -certreq -file cert-broker-request -storepass $PASS -keypass $PASS

iv. as the CA, sign the request.
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-broker-request -out cert-broker-signed -days 365 -CAcreateserial -passin pass:$PASS
-   keytool -printcert -v -file cert-broker-signed
-   keytool -list -v -keystore kafka.server.keystore.jks

v. import the CA public key, and the (now signed) broker public key into the keystore.
-   keytool -keystore kafka.server.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.server.keystore.jks -import -file cert-broker-signed -storepass $PASS -keypass $PASS -noprompt



vi. create the broker truststore and include our trust with our CA
-   keytool -keystore kafka.server.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


# Adjust server.properties. optionally, can spin up a kafka container and test the ssl connection with this :
-   openssl s_client -connect localhost:9093






# CLIENT-SIDE START. CREATE KEY/TRUST STORES FOR EACH. 

vii. console producer/consumer config. create trust store and add CA to it. If one were to stop after inputting this command, would be enough for client:broker one way ssl handshake.
-   keytool -keystore kafka.client.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt




viii. create client certificate/keystore.  
-   keytool -genkey -keystore kafka.client.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $clientAlias -storetype pkcs12



ix. cert request file for client && sign the request with CA.
-   keytool -keystore kafka.client.keystore.jks -certreq -file cert-client-request -alias $clientAlias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-client-request -out cert-client-signed -days 365 -CAcreateserial -passin pass:$PASS


x. import the CA public key and signed client public key into client keystore.
-   keytool -keystore kafka.client.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.client.keystore.jks -import -file cert-client-signed -alias $clientAlias -storepass $PASS -keypass $PASS -noprompt





# REST START.

xi. create rest trust store and add CA to it. 
-   keytool -keystore kafka.rest.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


xii. create rest certificate/keystore.
-   keytool -genkey -keystore kafka.rest.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $restAlias -storetype pkcs12



xiii. cert request file for rest public key && sign the request with CA.
-   keytool -keystore kafka.rest.keystore.jks -certreq -file cert-rest-request -alias $restAlias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-rest-request -out cert-rest-signed -days 365 -CAcreateserial -passin pass:$PASS



xiv. import the CA public key and signed rest public key into the rest keystore.
-   keytool -keystore kafka.rest.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.rest.keystore.jks -import -file cert-rest-signed -alias $restAlias -storepass $PASS -keypass $PASS -noprompt




==========================================================================================================================================

# TRANSFORMER START
xv. create transformer trust store and add CA to it.
-   keytool -keystore kafka.transformer.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


xvi. create transformer certificate/keystore
-   keytool -genkey -keystore kafka.transformer.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $transformerAlias -storetype pkcs12


xvii. cert request file for transformer public key && sign the request with CA.
-   keytool -keystore kafka.transformer.keystore.jks -certreq -file cert-transformer-request -alias $transformerAlias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-transformer-request -out cert-transformer-signed -days 365 -CAcreateserial -passin pass:$PASS


xviii. import the CA public key and signed transformer public key into the transformer keystore.
-   keytool -keystore kafka.transformer.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.transformer.keystore.jks -import -file cert-transformer-signed -alias $transformerAlias -storepass $PASS -keypass $PASS -noprompt


==========================================================================================================================================

# ROUTER START
xix. create router trust store and add CA to it.
-   keytool -keystore kafka.router.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


xx. create router certificate/keystore
-   keytool -genkey -keystore kafka.router.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $routerAlias -storetype pkcs12


xxi. cert request for router public key && sign the request with CA. 
-   keytool -keystore kafka.router.keystore.jks -certreq -file cert-router-request -alias $routerAlias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-router-request -out cert-router-signed -days 365 -CAcreateserial -passin pass:$PASS



xxii. import the CA public key and signed router key into the router keystore
-   keytool -keystore kafka.router.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.router.keystore.jks -import -file cert-router-signed -alias $routerAlias -storepass $PASS -keypass $PASS -noprompt




==========================================================================================================================================

# PERSISTENCE 1 START

xxiii. create persistence trust store and add CA to it.
-   keytool -keystore kafka.persistence.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt

xxiv. create persistence certificate/keystore.
-   keytool -genkey -keystore kafka.persistence.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $persistenceAlias -storetype pkcs12

xxv. cert request file for persistence public key && sign the request with CA.
-   keytool -keystore kafka.persistence.keystore.jks -certreq -file cert-persistence-request -alias $persistenceAlias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-persistence-request -out cert-persistence-signed -days 365 -CAcreateserial -passin pass:$PASS

xxvi. import the CA public key and signed persistence public key into the persistence keystore.
-   keytool -keystore kafka.persistence.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.persistence.keystore.jks -import -file cert-persistence-signed -alias $persistenceAlias -storepass $PASS -keypass $PASS -noprompt




==========================================================================================================================================
# PERSISTENCE 2 START

xxvii. create persistence2 trust store and add CA to it.
-   keytool -keystore kafka.persistence2.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


xxviii. create persistence2 certificate/keystore.
-   keytool -genkey -keystore kafka.persistence2.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $persistence2Alias -storetype pkcs12


xxix. cert request file for persistence2 public key && sign the request with CA.
-   keytool -keystore kafka.persistence2.keystore.jks -certreq -file cert-persistence2-request -alias $persistence2Alias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-persistence2-request -out cert-persistence2-signed -days 365 -CAcreateserial -passin pass:$PASS



xxx. import the CA public key and signed persistence2 public key into the persistence2 keystore.
-   keytool -keystore kafka.persistence2.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.persistence2.keystore.jks -import -file cert-persistence2-signed -alias $persistence2Alias -storepass $PASS -keypass $PASS -noprompt




==========================================================================================================================================
# SCHEMA REGISTRY START



xxxi. create schemaregistry trust store and add CA to it.
-   keytool -keystore kafka.schema.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


xxxii. create schema certificate/keystore.
-   keytool -genkey -keystore kafka.schema.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $schemaAlias -storetype pkcs12


xxxiii. cert request file for schemaregistry public key && sign the request with CA.
-   keytool -keystore kafka.schema.keystore.jks -certreq -file cert-schema-request -alias $schemaAlias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-schema-request -out cert-schema-signed -days 365 -CAcreateserial -passin pass:$PASS


xxxiv. import the CA public key and signed schemaregistry public key into the schemaregistry keystore.
-   keytool -keystore kafka.schema.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.schema.keystore.jks -import -file cert-schema-signed -alias $schemaAlias -storepass $PASS -keypass $PASS -noprompt







==========================================================================================================================================
# CONNECT START




xxxv. create connect trust store and add CA to it.
-   keytool -keystore kafka.connect.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


xxxvi. create connect certificate/keystore.
-   keytool -genkey -keystore kafka.connect.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $connectAlias -storetype pkcs12


xxxvii. cert request file for connect public key && sign the request with CA.
-   keytool -keystore kafka.connect.keystore.jks -certreq -file cert-connect-request -alias $connectAlias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-connect-request -out cert-connect-signed -days 365 -CAcreateserial -passin pass:$PASS


xxxviii. import the CA public key and signed connect public key into connect keystore.
-   keytool -keystore kafka.connect.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.connect.keystore.jks -import -file cert-connect-signed -alias $connectAlias -storepass $PASS -keypass $PASS -noprompt






==========================================================================================================================================



# KSQL START

xxxix. create ksql trust store and add CA to it.
-   keytool -keystore kafka.ksql.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


xl. create ksql certificate/keystore.
-   keytool -genkey -keystore kafka.ksql.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $ksqlAlias -storetype pkcs12


xli. cert request file for ksql public key && sign the request with CA.
-   keytool -keystore kafka.ksql.keystore.jks -certreq -file cert-ksql-request -alias $ksqlAlias -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-ksql-request -out cert-ksql-signed -days 365 -CAcreateserial -passin pass:$PASS


xlii. import the CA public key and signed ksql public key into the ksql keystore.
-   keytool -keystore kafka.ksql.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.ksql.keystore.jks -import -file cert-ksql-signed -alias $ksqlAlias -storepass $PASS -keypass $PASS -noprompt



















==========================================================================================================================================
# TEMPLATE
create <> trust store and add CA to it.
-   keytool -keystore kafka.<>.truststore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt


create <> certificate/keystore.
-   keytool -genkey -keystore kafka.<>.keystore.jks -validity 365 -storepass $PASS -dname "CN=localhost" -alias $<specific client alias : rest/transformer/etc> -storetype pkcs12


cert request file for <> public key && sign the request with CA.
-   keytool -keystore kafka.<>.keystore.jks -certreq -file cert-<>-request -alias $<> -storepass $PASS -keypass $PASS
-   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-<<>>-request -out cert-<<>>-signed -days 365 -CAcreateserial -passin pass:$PASS



import the CA public key and signed <> public key into the <> keystore.
-   keytool -keystore kafka.<>.keystore.jks -alias $CAA -import -file ca-cert -storepass $PASS -keypass $PASS -noprompt
-   keytool -keystore kafka.<>.keystore.jks -import -file cert-<>-signed -alias $<> -storepass $PASS -keypass $PASS -noprompt





