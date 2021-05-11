package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.library.pojos.TransferStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankBinderTest {

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    BankBinder bankBinder;


    @Test
    void givenInvalidTransferStatus_willReturnFormattedString() throws JsonProcessingException {
        TransferStatus status = TransferStatus.FAIL;
        when(objectMapper.writeValueAsString(any(TransferStatus.class))).thenThrow(JsonProcessingException.class);
        assertThat(bankBinder.toJson(status)).isEqualTo(String.format("{\n" + "   \"TransferStatus\": \"%s\"" + "\n}", status));
    }

}
