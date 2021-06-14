package tech.nermindedovic.rest.api.ksql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KsqlAPI.class)
@ExtendWith(MockitoExtension.class)
class KsqlAPITest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    KsqlService service;

    @Test
    void onGetRequest_kafkaTopics_returnsTopics() throws Exception {
        String expected = "Topic | Parition \n topic1 | 1";
        when(service.retrieveTopics()).thenReturn(expected);
        mockMvc.perform(get("/ksql/topics"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));

    }

    @Test
    void onGetRequest_errorCount_returnsErrorCountMessage() throws Exception {
        String expected = "Errors - 1";
        when(service.retrieveErrorCount()).thenReturn(expected);
        mockMvc.perform(get("/ksql/error-count"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }


    @Test
    void onGetRequest_bank1PCount_returnsCountMessage() throws Exception{
        String expected = "Bank (111) has successfully processed 1 transfer.";
        when(service.retrieveBank1PersistedCount()).thenReturn(expected);
        mockMvc.perform(get("/ksql/bank1/persisted-count"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }


    @Test
    void onGetRequest_bank2PCount_returnsCountMessage() throws Exception {
        String expected = "Bank (222) has successfully processed 1 transfer.";
        when(service.retrieveBank2PersistedCount()).thenReturn(expected);
        mockMvc.perform(get("/ksql/bank2/persisted-count"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }


}