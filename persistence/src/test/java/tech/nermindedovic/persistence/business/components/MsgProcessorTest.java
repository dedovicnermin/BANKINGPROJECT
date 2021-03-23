package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


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


    private XmlMapper mapper = new XmlMapper();

    private BalanceMessage createBalanceMsg(long accountNumber, long routingNum, String balance, boolean errors) {
        return new BalanceMessage(accountNumber, routingNum, balance, errors);
    }
}