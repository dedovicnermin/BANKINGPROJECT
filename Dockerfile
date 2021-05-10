FROM confluentinc/cp-kafka-connect
ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.1.1