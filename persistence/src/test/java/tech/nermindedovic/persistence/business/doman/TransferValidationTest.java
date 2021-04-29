package tech.nermindedovic.persistence.business.doman;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import tech.nermindedovic.persistence.data.entity.Account;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransferValidationTest {

    final ObjectMapper mapper = new ObjectMapper();
    @Test
    void toJsonString_willThrowOnInvalidTransferValidation() {
        TransferValidation transferValidation = new TransferValidation();
        assertThat(transferValidation.toJsonString()).isEqualTo("error");
    }


    @Test
    void toJsonString_willNotThrow_onValidTransferValidation() throws JsonProcessingException {
        TransferValidation transferValidation = new TransferValidation(773, BigDecimal.TEN, 1, "TransferMessage", new Account(32,23), new Account(33,643) );
        assertThat(transferValidation.toJsonString()).isEqualTo(mapper.writeValueAsString(transferValidation));
    }

}