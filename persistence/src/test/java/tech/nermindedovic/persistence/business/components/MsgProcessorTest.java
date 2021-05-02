package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.persistence.business.service.PersistenceService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void processTransferValidation() throws com.fasterxml.jackson.core.JsonProcessingException {
        //given valid TransferValidation

        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(5445L)
                .currentLeg(1)
                .creditorAccount(new Creditor(123, 111))
                .debtorAccount(new Debtor(456, 222))
                .transferMessage("A transferMessage")
                .amount(BigDecimal.TEN)
                .build();
        String json = mapper.writeValueAsString(transferValidation);

        doNothing().when(persistenceService).processTransferValidation(transferValidation);
        assertThat(msgProcessor.processTransferValidation(String.valueOf(transferValidation.getMessageId()),json)).isEqualTo(json);

    }

    @Test
    void processTransferValidation_fail() throws JsonProcessingException {
        TransferValidation transferValidation = new TransferValidation();
        String json = mapper.writeValueAsString(transferValidation);
        assertThat(msgProcessor.processTransferValidation("123", json)).isEqualTo(json);
    }


}