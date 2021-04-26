package tech.nermindedovic.persistence.business.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.TransferValidation;
import tech.nermindedovic.persistence.business.service.PersistenceService;
import tech.nermindedovic.persistence.data.entity.Account;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsgProcessorTest {

    @Mock
    PersistenceService persistenceService;

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    MsgProcessor msgProcessor;

    @Test
    void processBalanceRequest() {
        //given
        String balanceXml = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>111</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";

        //when
        doNothing().when(persistenceService).validateBalanceMessage(any(BalanceMessage.class));
        String actual = msgProcessor.processBalanceRequest(balanceXml);

        //then
        assertThat(actual).isEqualTo(balanceXml);
    }


    @Test
    void processBalanceRequest_fail() {
        //given invalid xml
        String balanceXML = "<Unbindable>";
        String expected = "<BalanceMessage><accountNumber>0</accountNumber><routingNumber>0</routingNumber><balance></balance><errors>true</errors></BalanceMessage>";

        //then
        assertThat(msgProcessor.processBalanceRequest(balanceXML)).isEqualTo(expected);
    }





    @Test
    void processTransferValidation() throws JsonProcessingException {
        //given valid TransferValidation
        ObjectMapper mapper = new ObjectMapper();
        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(5445)
                .currentLeg(1)
                .creditorAccount(new Account(123, 111))
                .debtorAccount(new Account(456, 222))
                .transferMessage("A transferMessage")
                .amount(BigDecimal.TEN)
                .build();
        String json = mapper.writeValueAsString(transferValidation);

        doNothing().when(persistenceService).processTransferValidation(transferValidation);
        assertThat(msgProcessor.processTransferValidation(json)).isEqualTo(json);


    }
}