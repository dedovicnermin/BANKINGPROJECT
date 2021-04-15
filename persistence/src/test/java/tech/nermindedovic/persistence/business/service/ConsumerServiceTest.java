package tech.nermindedovic.persistence.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.persistence.business.components.MsgProcessor;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ConsumerServiceTest {

    @Mock
    MsgProcessor msgProcessor;

    @InjectMocks
    private ConsumerService consumerService;

    @Test
    void onHandleBalanceRequest_withValidXML_returnsUpdatedBalanceMsg_asXML() throws JsonProcessingException {
        BalanceMessage balanceMessage = createBalanceMsg(1L, 1L, "", false);
        String xmlStart = mapper.writeValueAsString(balanceMessage);
        log.info(xmlStart);

        balanceMessage.setBalance("200.00");
        String xmlEnd = mapper.writeValueAsString(balanceMessage);
        log.info(xmlEnd);

        when(msgProcessor.processBalanceRequest(xmlStart)).thenReturn(xmlEnd);

        assertThat(consumerService.handleBalanceRequest(xmlStart)).isEqualTo(xmlEnd);

    }


    @Test
    void onHandleBalanceRequest_withInvalidXML_willHandleException() throws JsonProcessingException {
        String invalid = "<error></error>";
        when(msgProcessor.processBalanceRequest(invalid)).thenThrow(JsonProcessingException.class);

        BalanceMessage balanceMessage = createBalanceMsg(0, 0, "", true);
        String xmlOfEmptyMsg = mapper.writeValueAsString(balanceMessage);
        when(msgProcessor.processFailedAttempt(balanceMessage)).thenReturn(xmlOfEmptyMsg);


        String actual = consumerService.handleBalanceRequest(invalid);
        assertThat(xmlOfEmptyMsg).isEqualTo(actual);

    }

    @Test
    void onHandleFundsTransfer_withValidData_shouldNotThrow() throws JsonProcessingException, InvalidTransferMessageException {
        TransferMessage tm = new TransferMessage(12,new Creditor(123,456),new Debtor(567, 890), LocalDate.now(), new BigDecimal(25), "My memo");
        String xml = mapper.writeValueAsString(tm);

        doNothing().when(msgProcessor).processTransferRequest(xml);

        consumerService.handleFundsTransferRequest(xml);
        assertDoesNotThrow(() -> new RuntimeException());



    }

    @Test
    void onHandleFundsTransfer_withInvalidXML_shouldThrowRunTimeException() throws JsonProcessingException, InvalidTransferMessageException {
        String xml = "<ERROR></ERROR>";
        doThrow(InvalidTransferMessageException.class).when(msgProcessor).processTransferRequest(xml);

        assertThrows(RuntimeException.class, () -> consumerService.handleFundsTransferRequest(xml));
    }




    private XmlMapper mapper = new XmlMapper();

    private BalanceMessage createBalanceMsg(long accountNumber, long routingNum, String balance, boolean errors) {
        return new BalanceMessage(accountNumber, routingNum, balance, errors);
    }

}