package tech.nermindedovic.rest.api;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import tech.nermindedovic.rest.business.domain.BalanceMessage;
import tech.nermindedovic.rest.business.domain.TransferMessage;
import tech.nermindedovic.rest.kafka.balance.BalanceProducer;
import tech.nermindedovic.rest.kafka.transfer.TransferFundsProducer;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Slf4j
@RestController
public class RestAPI {



    private final BalanceProducer balanceProducer;
    private final TransferFundsProducer transferFundsProducer;
    private final WebClient webClient;

    public RestAPI(final BalanceProducer balanceProducer, final TransferFundsProducer transferFundsProducer, final WebClient webClient) {
        this.balanceProducer = balanceProducer;
        this.transferFundsProducer = transferFundsProducer;
        this.webClient = webClient;
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
        Optional<ResponseEntity<String>> response = webClient
                .get()
                .uri("/" + key)
                .retrieve()
                .toEntity(String.class)
                .blockOptional();
        if (response.isPresent()) return response.get().getBody();
        else return "ID " + key + " is not valid";
    }












}
