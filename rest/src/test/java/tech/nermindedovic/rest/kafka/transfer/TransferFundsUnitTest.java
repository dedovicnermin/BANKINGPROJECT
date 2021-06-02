package tech.nermindedovic.rest.kafka.transfer;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tech.nermindedovic.AvroCreditor;
import tech.nermindedovic.AvroDebtor;
import tech.nermindedovic.AvroTransferMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TransferFundsUnitTest {

    @Mock
    KafkaTemplate<String, AvroTransferMessage> kafkaTemplateMock;


    long msgId = 34235525;
    long creditorAN = 333, creditorRN = 111;
    Creditor creditor = new Creditor(creditorAN, creditorRN);
    long debtorAN = 345, debtorRN = 222;
    Debtor debtor = new Debtor(debtorAN, debtorRN);
    LocalDate date = LocalDate.now();
    BigDecimal amount = BigDecimal.TEN;
    String memo = "test memo";





    @Test
    void givenPojo_returnsAvroTransferMessage() {
        TransferFundsProducer producer = new TransferFundsProducer(kafkaTemplateMock);
        TransferMessage input = new TransferMessage(msgId, creditor, debtor, date, amount, memo);

        AvroTransferMessage expected = new AvroTransferMessage(msgId, new AvroCreditor(creditorAN,creditorRN), new AvroDebtor(debtorAN,debtorRN), date.toString(), amount.toString(),memo);
        assertThat(producer.toAvro(input)).isEqualTo(expected);
    }
}
