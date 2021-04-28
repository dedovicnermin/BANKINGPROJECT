# BANKINGPROJECT  
[![Build Status](https://travis-ci.com/dedovicnermin/BANKINGPROJECT.svg?branch=master)](https://travis-ci.com/dedovicnermin/BANKINGPROJECT)

[![codecov](https://codecov.io/gh/dedovicnermin/BANKINGPROJECT/branch/master/graph/badge.svg?token=3STMF7Q3L3)](https://codecov.io/gh/dedovicnermin/BANKINGPROJECT)

## End points:
1. /balance - returns the current balance
2. /funds/transfer - sends a request to transfer funds between two users

### Rest application
Responsible for starting the chain of message driven events. Current endpoints:
* /balance
  *  given valid accountNumber + routingNumber, returns current balance in bank
  *  sends message to transformer application as JSON pojo to be transformed into an xml string
  *  synchronous request/reply using Kafka's **ReplyingKafkaTemplate** 

* /funds/transfer
  * given a valid transfer message, will persist transaction and update the balance for both accounts
  * request returns status on wether or not the message was able to successfully be sent
    *  AKA does it reach the kafka broker successfully
  * rest response only guarentees that transfer message has been successfully sent, and does not guarentee the transfer request will go through
  * a consumer for errors found in persistence application on invalid transferMessage requests
 
 
 
 ### Transformer application
 Responsible for transforming POJO's into xml strings and vice versa
 * on balance request messages:
   * listens/consumes balance message sent from REST app
   * Transformes message to xml string using **XmlMapper** 
   * produces synchronous request/reply message using **ReplyingKafkaTemplate** and waits for the response from persistence application
   * gets response (both failed and successful) from persistence app and transforms the xml string back to the **BalanceMessage** pojo before sending back to REST
 * on transfer funds request messages:
   * listens/consumes transfer message from REST app
   * transformes message to xml string
   * produces message with the xml value to persistence and only confirms wether or not the message successfully reached the broker 
   * sends errors to the dead letter topic
 
 
 ### Persistence application
 Responsible for validating and processing requests
 * on balance request messages:
   * listens/consumes XML strings sent from transformer app
   * binds xml to POJO
   * validates account information 
     * catches invalid information and responds with message that includes a triggered errors flag
   * produces back to Transformer app (who is waiting for reply) 
   * converts POJO back to xml string before sending
 * on transfer funds request messages:
   * consumes xml string and attempts to bind to POJO
   * validates debtor/creditor accounts as well as ensure the transferMessage doesn't contain invalid values
   * processes the transaction upon validation - entering transaction + updating balance
   * idempotent consumer
   * sends errors to dead letter topic 




#### TransferMessage Object
Skeleton of TransferMessage
```
<TransferMessage>
  <message/transaction id>
  <creditor>
    <accountNumber>valid accountNumber that can be found in db</accountNumber>
    <routingNumber>valid routingNumber that can be found in db<routingNumber>
  </creditor>
  <debtor>
    <accountNumber>valid accountNumber that can be found in db</accountNumber>
    <routingNumber>valid routingNumber that can be found in db<routingNumber>
  </debtor>
  <amount>number greater than 0</amount>
  <date>date of transaction</date>
  <memo>describing purpose of transaction</memo>
</TransferMessage>      
```
      
