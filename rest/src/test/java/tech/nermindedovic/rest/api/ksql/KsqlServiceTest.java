package tech.nermindedovic.rest.api.ksql;

import io.confluent.ksql.api.client.BatchedQueryResult;
import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.Row;
import io.confluent.ksql.api.client.TopicInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KsqlServiceTest {

    @Mock
    Client client;

    @Mock
    CompletableFuture<List<TopicInfo>> futureMock;

    @Mock
    BatchedQueryResult queryResultMock;


    @Mock
    Row pullResultMock;

    private static final String TEMPLATE_HEADER = "%75s%5s%5s%n";
    private static final String TEMPLATE_BODY = "%75s%5s%5d%n";


    @Test
    void willFormatCorrectly_whenRetrievingKafkaTopics() throws ExecutionException, InterruptedException {
        String topic1 = "topic1";
        String topic2 = "topic2";
        String topic3 = "topic3";


        TopicInfo topicInfo1 = Mockito.mock(TopicInfo.class);
        TopicInfo topicInfo2 = Mockito.mock(TopicInfo.class);
        TopicInfo topicInfo3 = Mockito.mock(TopicInfo.class);
        List<TopicInfo> topicInfos = Arrays.asList(topicInfo1, topicInfo2, topicInfo3);

        KsqlService service = new KsqlService(client);

        when(client.listTopics()).thenReturn(futureMock);
        when(futureMock.get()).thenReturn(topicInfos);
        when(topicInfo1.getName()).thenReturn(topic1);
        when(topicInfo2.getName()).thenReturn(topic2);
        when(topicInfo3.getName()).thenReturn(topic3);
        when(topicInfo1.getPartitions()).thenReturn(1);
        when(topicInfo2.getPartitions()).thenReturn(1);
        when(topicInfo3.getPartitions()).thenReturn(1);

        String expected = String.format(TEMPLATE_HEADER, "Topic", "|", "Partitions") +
                String.format(TEMPLATE_BODY, topic1, "|", 1) +
                String.format(TEMPLATE_BODY, topic2, "|", 1) +
                String.format(TEMPLATE_BODY, topic3, "|", 1);



        assertEquals(expected, service.retrieveTopics());

    }


    @Test
    void onClientException_willReturnErrorString_whenRetrievingTopics() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        when(client.listTopics()).thenReturn(futureMock);
        when(futureMock.get()).thenThrow(ExecutionException.class);
        assertThat(service.retrieveTopics()).contains("Error!");
    }


    @Test
    void whenQueryingTopics_onEmptyList_returnsMessageConfirming() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        when(client.listTopics()).thenReturn(futureMock);
        when(futureMock.get()).thenReturn(Collections.emptyList());

        assertThat(service.retrieveTopics()).isEqualTo("There is no data for this query at the moment.");

    }


    // ERROR COUNT TESTS

    @Test
    void whenRetrievingErrorCount_willReturnCountMessage() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        List<Row> rows = Collections.singletonList(pullResultMock);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenReturn(rows);
        when(pullResultMock.getInteger(any())).thenReturn(1);

        assertThat(service.retrieveErrorCount()).endsWith("1");
    }


    @Test
    void whenRetrievingErrorCount_emptyResults_willReturnStringWithSuchMessage() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenReturn(Collections.emptyList());

        assertThat(service.retrieveErrorCount()).isEqualTo("No errors have been logged yet!");
    }


    @Test
    void whenRetrievingErrorCount_exceptionThrown_willReturnStringWithSuchMessage() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenThrow(ExecutionException.class);

        assertThat(service.retrieveErrorCount()).isEqualTo("Error - Could not execute query to client.");
    }



    // BANK 1 PERSISTED COUNT
    @Test
    void whenRetrievingBank1PersistedCount_willReturnMessageWithCount() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        List<Row> rows = Collections.singletonList(pullResultMock);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenReturn(rows);
        when(pullResultMock.getInteger(any())).thenReturn(1);

        assertThat(service.retrieveBank1PersistedCount()).contains("Bank with routing (111) has successfully processed 1 transfer(s).");
    }


    @Test
    void whenRetrievingBank1PersistedCount_onEmptyList_willReturnWithSuchMessage() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenReturn(Collections.emptyList());

        assertThat(service.retrieveBank1PersistedCount()).isEqualTo("Did not find any persisted transfers.");
    }


    @Test
    void whenRetrievingBank1PersistedCount_onException_willReturnErrorMessage() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenThrow(ExecutionException.class);

        assertThat(service.retrieveBank1PersistedCount()).isEqualTo("Error - Could not execute query to client.");
    }




    // BANK 2 PERSISTED COUNT
    @Test
    void whenRetrievingBank2PersistedCount_willReturnMessageWithCount() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        List<Row> rows = Collections.singletonList(pullResultMock);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenReturn(rows);
        when(pullResultMock.getInteger(any())).thenReturn(1);

        assertThat(service.retrieveBank2PersistedCount()).contains("Bank with routing (222) has successfully processed 1 transfer(s).");


    }


    @Test
    void whenRetrievingBank2PersistedCount_onEmptyList_willReturnWithSuchMessage() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenReturn(Collections.emptyList());

        assertThat(service.retrieveBank2PersistedCount()).isEqualTo("Did not find any persisted transfers.");
    }


    @Test
    void whenRetrievingBank2PersistedCount_onException_willReturnErrorMessage() throws ExecutionException, InterruptedException {
        KsqlService service = new KsqlService(client);
        when(client.executeQuery(any())).thenReturn(queryResultMock);
        when(queryResultMock.get()).thenThrow(ExecutionException.class);

        assertThat(service.retrieveBank1PersistedCount()).isEqualTo("Error - Could not execute query to client.");
    }


    // SINGLE BANK ATTEMPT EVENTS








}