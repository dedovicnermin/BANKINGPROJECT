package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MsgProcessorTest {

    @Mock
    XMLProcessor xmlProcessor;

    @InjectMocks
    MsgProcessor msgProcessor;


    @Test
    void processBalanceRequest() throws JsonProcessingException {
        BalanceMessage balanceMessageWithoutBalance = createBalanceMsg(21L, 2L, "", false);
        String xmlWithoutBal = mapper.writeValueAsString(balanceMessageWithoutBalance);

        BalanceMessage balanceMessageWithBalance = createBalanceMsg(21L, 2L, "12.99", false);
        String xmlWithBal = mapper.writeValueAsString(balanceMessageWithBalance);

        when(xmlProcessor.bindAndValidateBalanceRequest(xmlWithoutBal)).thenReturn(xmlWithBal);

        assertThat(msgProcessor.processBalanceRequest(xmlWithoutBal)).isEqualTo(xmlWithBal);
    }

    @Test
    void process_onInvalid_xml() throws JsonProcessingException {
        String xml = "<error></error>";
        when(xmlProcessor.bindAndValidateBalanceRequest(xml)).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> msgProcessor.processBalanceRequest(xml));
    }



    @Test
    void onFailedAttempt_whenGivenEmptyBalanceMessage_returnsXMLWithErrorSetToTrue() throws JsonProcessingException{

        BalanceMessage balanceMessage = createBalanceMsg(0, 0, "", true);
        String expected = mapper.writeValueAsString(balanceMessage);

        when(xmlProcessor.convertEmptyBalanceMessage(balanceMessage)).thenReturn(expected);

        assertThat(msgProcessor.processFailedAttempt(balanceMessage)).isEqualTo(expected);
    }




    @Test
    void test_onValidXMLString_returnsNoErrors() throws JsonProcessingException, InvalidTransferMessageException {
        Debtor debtor = new Debtor(123, 456);
        Creditor creditor = new Creditor(456,789);
        TransferMessage transferMessage = new TransferMessage(1111,creditor,debtor, new Date(), BigDecimal.TEN, "for war");

        String xml = mapper.writeValueAsString(transferMessage);


        doNothing().when(xmlProcessor).bindAndProcessTransferRequest(xml);

        assertDoesNotThrow(() -> msgProcessor.processTransferRequest(xml));

    }

    @Test
    void test_onInvalidXMLString_throwsJsonException() throws JsonProcessingException, InvalidTransferMessageException {
        String xml = "<ERROR></ERROR>";

        doThrow(InvalidTransferMessageException.class).when(xmlProcessor).bindAndProcessTransferRequest(xml);

        assertThrows(InvalidTransferMessageException.class, () -> msgProcessor.processTransferRequest(xml));
    }



    private XmlMapper mapper = new XmlMapper();

    private BalanceMessage createBalanceMsg(long accountNumber, long routingNum, String balance, boolean errors) {
        return new BalanceMessage(accountNumber, routingNum, balance, errors);
    }
}