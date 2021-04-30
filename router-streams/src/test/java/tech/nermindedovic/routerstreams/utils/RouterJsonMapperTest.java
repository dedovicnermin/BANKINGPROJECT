package tech.nermindedovic.routerstreams.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;
import tech.nermindedovic.routerstreams.business.domain.Account;
import tech.nermindedovic.routerstreams.business.domain.TransferValidation;
import tech.nermindedovic.routerstreams.config.BeanConfig;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {BeanConfig.class, RouterJsonMapper.class})
@ExtendWith(MockitoExtension.class)
class RouterJsonMapperTest {

    @Spy
    ObjectMapper objectMapper;


    @InjectMocks
    RouterJsonMapper mapper;


    @Test
    void toJsonString_errors() throws JsonProcessingException {
        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(101L)
                .transferMessage("Mock transfer message")
                .currentLeg(1)
                .amount(BigDecimal.TEN)
                .creditorAccount(new Account(1222, 222))
                .debtorAccount(new Account(2323, 111))
                .build();

        when(objectMapper.writeValueAsString(transferValidation)).thenThrow(JsonProcessingException.class);

        assertThat(mapper.toJsonString(transferValidation)).isEqualTo(transferValidation.toString());


    }
}