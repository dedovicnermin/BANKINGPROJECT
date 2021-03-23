package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.service.PersistenceService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XMLProcessorTest {

    @Mock
    PersistenceService persistenceService;

    @InjectMocks
    private XMLProcessor xmlProcessor;

    @Test
    void bindAndValidateBalanceRequest_OnValid_XML() throws JsonProcessingException {
        BalanceMessage message = createBalanceMsg(1L, 1L, "200.00", false);
        String xml = toXml(message);


        doAnswer(invocationOnMock -> {
            BalanceMessage message1 = invocationOnMock.getArgument(0);
            message1.setErrors(false);
            message1.setBalance("200.00");

            assertFalse(message1.getErrors());
            return null;
        }).when(persistenceService).validateBalanceMessage(message);

        xmlProcessor.bindAndValidateBalanceRequest(xml);


        verify(persistenceService, times(1)).validateBalanceMessage(message);

        String actualXml = xmlProcessor.bindAndValidateBalanceRequest(xml);
        assertThat(message.getErrors()).isEqualTo(false);
        assertThat(message.getBalance()).isNotEmpty();
        assertThat(actualXml).isEqualTo(xml);
    }





    @Test
    void convertToXml_onValidXML_test() throws JsonProcessingException {
        BalanceMessage message = new BalanceMessage(1L, 1L, "19.28", false);
        String expectedXml = mapper.writeValueAsString(message);

        assertThat(xmlProcessor.convertToXml(message)).isEqualTo(expectedXml);
    }


    @Test
    void test_convertEmptyBalanceMessage_onEmptyMessage_willReturn_string() throws JsonProcessingException {
        BalanceMessage balanceMessage = createBalanceMsg(0,0,"", true);
        String expected = mapper.writeValueAsString(balanceMessage);

        assertThat(xmlProcessor.convertEmptyBalanceMessage(balanceMessage)).isEqualTo(expected);

    }


    private BalanceMessage createBalanceMsg(long accountNumber, long routingNum, String balance, boolean errors) {
        return new BalanceMessage(accountNumber, routingNum, balance, errors);
    }


    private XmlMapper mapper = new XmlMapper();
    private String toXml(BalanceMessage balanceMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(balanceMessage);
    }
}