package tech.nermindedovic.rest.api;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.nermindedovic.rest.business.domain.BalanceMessage;
import tech.nermindedovic.rest.business.domain.TransferMessage;
import tech.nermindedovic.rest.kafka.balance.BalanceProducer;
import tech.nermindedovic.rest.kafka.transfer.TransferFundsProducer;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;


@Slf4j
@RestController
public class RestAPI {


    private BalanceProducer balanceProducer;
    private TransferFundsProducer transferFundsProducer;

    public RestAPI(final BalanceProducer balanceProducer, final TransferFundsProducer transferFundsProducer) {
        this.balanceProducer = balanceProducer;
        this.transferFundsProducer = transferFundsProducer;
    }


    @PostMapping(value = "balance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BalanceMessage getBalanceUpdate(@RequestBody @Valid BalanceMessage balanceMessage) throws ExecutionException, InterruptedException {
        return balanceProducer.sendAndReceive(balanceMessage);
    }


    @PostMapping(value = "funds/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String fundsTransferRequest(@RequestBody @Valid TransferMessage transferMessage) throws ExecutionException, InterruptedException {
        String result = transferFundsProducer.sendTransferMessage(transferMessage);
        return result;
    }









//          AtomicInteger counter = new AtomicInteger(1);
//        record.headers().add(new RecordHeader(KafkaHeaders.CORRELATION_ID, ByteBuffer.allocateDirect(getCounterValue())));
}
