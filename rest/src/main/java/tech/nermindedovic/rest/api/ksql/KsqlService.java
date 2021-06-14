package tech.nermindedovic.rest.api.ksql;

import io.confluent.ksql.api.client.BatchedQueryResult;
import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.Row;
import io.confluent.ksql.api.client.TopicInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;




import java.util.List;
import java.util.concurrent.ExecutionException;


@Service
@Slf4j
public class KsqlService {

    private static final long BANK1_ROUTING = 111;
    private static final long BANK2_ROUTING = 222;
    private static final String COUNT_COLUMN = "COUNT";

    //pull queries
    private static final String ERRORS_PQ = "select count from error_count_table where keycol = 1;";
    private static final String BANK1_PERSISTED_COUNT_PQ = "SELECT BANK, COUNT FROM B1_TABLE WHERE ORIGIN='postgres.transactions';";
    private static final String BANK2_PERSISTED_COUNT_PQ = "SELECT BANK, COUNT FROM B2_TABLE WHERE ORIGIN='postgres2.transactions';";

    //topic templates
    private static final String TEMPLATE_HEADER = "%75s%5s%5s%n";
    private static final String TEMPLATE_BODY = "%75s%5s%5d%n";


    private final Client client;
    public KsqlService(final Client client) { this.client = client; }


    public String retrieveTopics() {
        try {
            return formatTopics(getListOfTopics());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return "Error! Could not retrieve list of topics from KSQL Client.";
        }
    }


    public String retrieveErrorCount()  {
        List<Row> rows;
        try {
            rows = client.executeQuery(ERRORS_PQ).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            return "Error - Could not execute query to client.";
        }

        if (rows.isEmpty()) {
            return "No errors have been logged yet!";
        }

        Row pullResult = rows.get(0);
        Integer count = pullResult.getInteger(COUNT_COLUMN);
        return "Errors - " + count;
    }



    public String retrieveBank1PersistedCount()  {
        return persistedTransfersResponseFormatter(BANK1_PERSISTED_COUNT_PQ, BANK1_ROUTING);
    }

    public String retrieveBank2PersistedCount()  {
        return persistedTransfersResponseFormatter(BANK2_PERSISTED_COUNT_PQ, BANK2_ROUTING);
    }





    /**
     * Response formatter for listing Kafka topics
     * @param topics kafka topics
     * @return formatted response string
     */
    private String formatTopics(List<TopicInfo> topics) {
        if (topics == null || topics.isEmpty()) {
            return "There is no data for this query at the moment.";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(TEMPLATE_HEADER, "Topic", "|", "Partitions"));
        for (TopicInfo topic : topics) {
            String format = String.format(TEMPLATE_BODY, topic.getName(), "|", topic.getPartitions());
            builder.append(format);
        }
        return builder.toString();
    }





    /**
     *
     * @return A list of topic metadata.
     * @throws ExecutionException executionException
     * @throws InterruptedException interruptedException
     */
    private List<TopicInfo> getListOfTopics() throws ExecutionException, InterruptedException {
        return client.listTopics().get();
    }



    /**
     * Formatter for persisted count pull queries.
     * @param pullQuery ksql to execute
     * @param routingNumber bank/persistence reference
     * @return formatted response
     */
    private String persistedTransfersResponseFormatter(String pullQuery, long routingNumber)  {
        BatchedQueryResult batchedQueryResult = client.executeQuery(pullQuery);
        List<Row> rows;
        try {
            rows = batchedQueryResult.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            return "Error - Could not execute query to client.";
        }

        if (rows.isEmpty()) {
            return "Did not find any persisted transfers.";
        }

        Row row = rows.get(0);
        Integer count = row.getInteger(COUNT_COLUMN);
        return String.format("Bank with routing (%d) has successfully processed %d transfer(s).", routingNumber, count);
    }








}
