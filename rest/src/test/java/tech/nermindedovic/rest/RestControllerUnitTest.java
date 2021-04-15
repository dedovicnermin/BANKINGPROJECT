package tech.nermindedovic.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tech.nermindedovic.rest.api.RestAPI;

import tech.nermindedovic.rest.business.domain.BalanceMessage;
import tech.nermindedovic.rest.business.domain.Creditor;
import tech.nermindedovic.rest.business.domain.Debtor;
import tech.nermindedovic.rest.business.domain.TransferMessage;
import tech.nermindedovic.rest.kafka.balance.BalanceProducer;
import tech.nermindedovic.rest.kafka.transfer.TransferFundsProducer;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestAPI.class)
@ExtendWith(MockitoExtension.class)
class RestControllerUnitTest {

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TransferFundsProducer transferFundsProducer;

    @MockBean
    BalanceProducer balanceProducer;


    @Test
    void postTransferMessageEvent_withValidTransferMessage() throws Exception {
        //given
        TransferMessage transferMessage = TransferMessage.builder()
                .message_id(0)
                .creditor(new Creditor(1L, 23435352345L))
                .debtor(new Debtor(2L, 34454534534L))
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .memo("FOR LUNCH")
                .build();
        String json = mapper.writeValueAsString(transferMessage);

        //when
        when(transferFundsProducer.sendTransferMessage(any(TransferMessage.class))).thenReturn("Message has been sent successfully");

        //then
        mockMvc.perform(post("/funds/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Message has been sent successfully"));
    }


    @Test
    void givenInvalidTransferMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/funds/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
                .andExpect(status().isBadRequest());
    }




    @Test
    void givenVALIDBalanceMessage_returnsBalanceMessageWithBalanceUpdated() throws Exception {
        //given
        BalanceMessage balanceMessage = new BalanceMessage(1111,2222,"", false);
        String balanceMessageAsJson = mapper.writeValueAsString(balanceMessage);

        BalanceMessage balanceMessageReturn = new BalanceMessage(1111,2222,"100.50", false);

        //when
        when(balanceProducer.sendAndReceive(balanceMessage)).thenReturn(balanceMessageReturn);

        //then
        mockMvc.perform(post("/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(balanceMessageAsJson))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(balanceMessageReturn)));
    }


    @Test
    void givenInvalidBalanceMessage_willReturnBadRequest() throws Exception {
        mockMvc.perform(post("/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }
}
