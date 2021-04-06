package tech.nermindedovic.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.nermindedovic.rest.api.RestAPI;
import tech.nermindedovic.rest.business.domain.BalanceMessage;
import tech.nermindedovic.rest.business.domain.Creditor;
import tech.nermindedovic.rest.business.domain.Debtor;
import tech.nermindedovic.rest.business.domain.TransferMessage;
import tech.nermindedovic.rest.kafka.balance.BalanceProducer;
import tech.nermindedovic.rest.kafka.transfer.TransferFundsProducer;

import javax.websocket.SendResult;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestAPI.class)
@AutoConfigureMockMvc
public class RestControllerUnitTest {

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TransferFundsProducer transferFundsProducer;

    @MockBean
    BalanceProducer balanceProducer;

    @InjectMocks
    RestAPI restAPI;

    @Test
    void postTransferMessageEvent() throws Exception {
        //given
        TransferMessage transferMessage = TransferMessage.builder()
                .message_id(UUID.randomUUID().getLeastSignificantBits())
                .creditor(new Creditor(1L, 23435352345L))
                .debtor(new Debtor(2L, 34454534534L))
                .amount(new BigDecimal("100.00"))
                .date(new Date())
                .memo("FOR LUNCH")
                .build();

        String json = mapper.writeValueAsString(transferMessage);

        //when
//        when(transferFundsProducer.sendTransferMessage()).thenReturn(new ListenableFuture<SendResult<String>>)

        //then
        mockMvc.perform(post("/funds/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Funds transfer request sent."));


    }
}
