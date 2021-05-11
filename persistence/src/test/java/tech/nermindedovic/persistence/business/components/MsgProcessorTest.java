package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tech.nermindedovic.library.pojos.*;
import tech.nermindedovic.persistence.business.service.PersistenceService;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class MsgProcessorTest {

    @Mock
    PersistenceService persistenceService;

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;



    @Mock
    BankBinder bankBinder;

    @InjectMocks
    MsgProcessor msgProcessor;


    @Test
    void processBalanceRequest() throws JsonProcessingException {
        //given
        String balanceXml = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>111</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";

        //when
        BalanceMessage balanceMessage = new BalanceMessage(123, 111, "", false);
        when(bankBinder.toBalanceMessage(balanceXml)).thenReturn(balanceMessage);
        doNothing().when(persistenceService).validateBalanceMessage(any(BalanceMessage.class));
        when(bankBinder.toXml(balanceMessage)).thenReturn(balanceXml);


        String actual = msgProcessor.processBalanceRequest(balanceXml);

        //then
        assertThat(actual).isEqualTo(balanceXml);
    }


    @Test
    void processBalanceRequest_fail() throws JsonProcessingException {
        //given invalid xml
        String balanceXML = "<Unbindable>";
        String expected = "<BalanceMessage><accountNumber>0</accountNumber><routingNumber>0</routingNumber><balance></balance><errors>true</errors></BalanceMessage>";

        when(bankBinder.toBalanceMessage(balanceXML)).thenThrow(JsonProcessingException.class);

        //then
        assertThat(msgProcessor.processBalanceRequest(balanceXML)).isEqualTo(expected);
    }
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void processTransferValidation() throws JsonProcessingException {
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

        when(bankBinder.toTransferValidation(json)).thenReturn(transferValidation);
        doNothing().when(persistenceService).processTransferValidation(transferValidation);
        when(bankBinder.toJson(transferValidation)).thenReturn(json);

        assertThat(msgProcessor.processTransferValidation(String.valueOf(transferValidation.getMessageId()),json)).isEqualTo(json);

    }


    @Test
    void processTransferValidation_fail() throws JsonProcessingException {

        TransferValidation transferValidation = new TransferValidation();
        String json = mapper.writeValueAsString(transferValidation);
        when(bankBinder.toTransferValidation(json)).thenThrow(JsonProcessingException.class);

        when(bankBinder.toJson(any(TransferStatus.class))).thenReturn(String.format("{\n" + "   \"TransferStatus\": \"%s\"" + "\n}", TransferStatus.FAIL));

        assertThat(msgProcessor.processTransferValidation("123", json)).isEqualTo(json);
    }


    @Test
    void processTransferRequest_TwoBanks_onBindingFail_willSendToErrorTopic() throws JsonProcessingException {
        String xml = "XML that is invalid";
        when(bankBinder.toTransferMessage(xml)).thenThrow(JsonProcessingException.class);

        msgProcessor.processTransferRequestTwoBanks(xml);
        verify(kafkaTemplate,times(1)).send("funds.transfer.error","PERSISTENCE --- Unable to bind XML to TransferMessagePOJO");
    }

    @Test
    void processTransferRequest_twoBanks_onInvalidTransferMessage_willSendToErrorTopic() throws JsonProcessingException, InvalidTransferMessageException {
        long creditorAN, creditorRN;
        long messageId = creditorAN = creditorRN = 100L;
        long debtorAN = 1L, debtorRN = 111L;


        TransferMessage transferMessage = TransferMessage.builder()
                .messageId(messageId)
                .creditor(new Creditor(creditorAN, creditorRN))
                .debtor(new Debtor(debtorAN,debtorRN))
                .date(LocalDate.now())
                .amount(new BigDecimal("1.00"))
                .memo("Here's one dollar")
                .build();
        String xml = mapper.writeValueAsString(transferMessage);

        when(bankBinder.toTransferMessage(xml)).thenReturn(transferMessage);
        doThrow(InvalidTransferMessageException.class).when(persistenceService).processTwoBankTransferMessage(transferMessage, debtorAN, true);

        msgProcessor.processTransferRequestTwoBanks(xml);

        verify(kafkaTemplate, times(1)).send("funds.transfer.error", "PERSISTENCE --- TransferMessage already exists");

    }



}