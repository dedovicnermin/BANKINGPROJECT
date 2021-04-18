package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.business.service.PersistenceService;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("=== MESSAGE PROCESSOR TEST ===")

@ExtendWith({MockitoExtension.class})
class MsgProcessorTest {

    @Mock
    PersistenceService persistenceService;


    @InjectMocks
    MsgProcessor msgProcessor;

    XmlMapper mapper = new XmlMapper();

    @Test
    @DisplayName("MsgProcessor.processBalanceRequest ( GOOD XML )")
    void givenBindableXML_binds_thenPopulates_thenBindsBackToString_test() throws JsonProcessingException {
        //given a valid balance message
        BalanceMessage VALID = new BalanceMessage(1111,1111,"",false);
        String VALID_XML = mapper.writeValueAsString(VALID);

        String EXPECTED = mapper.writeValueAsString(new BalanceMessage(1111,1111,"10.00", false));


        //when passes persistenceService layer populates balance
        doAnswer((invocationOnMock -> {
            BalanceMessage balanceMessage = invocationOnMock.getArgument(0);
            balanceMessage.setBalance("10.00");
            return balanceMessage;
        })).when(persistenceService).validateBalanceMessage(VALID);


        // returns xml with balance included
        String ACTUAL = msgProcessor.processBalanceRequest(VALID_XML);
        assertThat(ACTUAL).isEqualTo(EXPECTED);
    }



    @Test
    @DisplayName("MsgProcessor.processBalanceRequest ( BAD XML )")
    void givenUnbindableXML_returnsBalanceMessageWithErrorsPopulated_test() {
        //given an unbindable balance xml
        String bad_xml = "<unbindable>";

        //will return error populated balance message
        assertThat(msgProcessor.processBalanceRequest(bad_xml)).isEqualTo("<BalanceMessage><accountNumber>0</accountNumber><routingNumber>0</routingNumber><balance></balance><errors>true</errors></BalanceMessage>");
    }



    @Test
    @DisplayName("MsgProcessor.processTransferMessage ( GOOD XML )")
    void givenBindableXML_ofTransferMessage_willHandOffToPersistenceForWork() throws JsonProcessingException, InvalidTransferMessageException {
        //given bindable transfer message
        TransferMessage transferMessage = new TransferMessage(10000000, new Creditor(10, 10), new Debtor(20, 20), LocalDate.now(), BigDecimal.TEN, "A good memo");
        String VALID = mapper.writeValueAsString(transferMessage);

        // persistence does what it has to do
        doNothing().when(persistenceService).validateAndProcessTransferMessage(transferMessage);

        // will not throw
        assertDoesNotThrow(() -> msgProcessor.processTransferRequest(VALID));
    }















}