package tech.nermindedovic.rest.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import tech.nermindedovic.library.avro.BalanceMessage;
import tech.nermindedovic.library.avro.TransferMessage;
import tech.nermindedovic.rest.api.elastic.BankTransaction;
import tech.nermindedovic.rest.kafka.balance.avro.BalanceProducer;
import tech.nermindedovic.rest.kafka.transfer.avro.TransferFundsProducer;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@Profile("avro")
public class RestAvroAPI {
    private final BalanceProducer balanceProducer;
    private final TransferFundsProducer transferFundsProducer;
    private final WebClient webClient;
    private final TransactionSearchService elasticService;



    public RestAvroAPI(final BalanceProducer balanceProducer, final TransferFundsProducer transferFundsProducer, final WebClient webClient, final TransactionSearchService transactionSearchService) {
        this.balanceProducer = balanceProducer;
        this.transferFundsProducer = transferFundsProducer;
        this.webClient = webClient;
        this.elasticService = transactionSearchService;
    }


    @PostMapping(value = "balance", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BalanceMessage getBalanceUpdate(@RequestBody @Valid BalanceMessage balanceMessage) {
        try {
            return balanceProducer.sendAndReceive(balanceMessage);
        } catch (ExecutionException | InterruptedException exception) {
            balanceMessage.setErrors(true);
            return balanceMessage;
        }
    }



    @PostMapping(value = "funds/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String fundsTransferRequest( @RequestBody @Valid TransferMessage transferMessage) throws ExecutionException, InterruptedException {
        transferMessage.setMessageId(Math.abs(UUID.randomUUID().getMostSignificantBits()));
        return transferFundsProducer.sendTransferMessage(transferMessage);
    }


    @GetMapping(value = "transfer/status/{key}")
    public String getTransferStatus(@PathVariable final String key) {
        Optional<ResponseEntity<String>> response = webClient.get().uri("/" + key).retrieve().toEntity(String.class).blockOptional();
        if (response.isPresent()) return response.get().getBody();
        else return "ID " + key + " is not valid";
    }


    @GetMapping(value = "transactions")
    public SearchHits<BankTransaction> getAllTransactions() {
        return elasticService.retrieveAllTransactions();
    }

}
