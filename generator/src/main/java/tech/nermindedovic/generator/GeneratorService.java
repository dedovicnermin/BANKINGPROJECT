package tech.nermindedovic.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.GsonMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class GeneratorService {

    private static final long BANK_A_ROUTING = 111;
    private static final long BANK_B_ROUTING = 222;



    private final int accountsPerBank;
    private final int totalAccounts;
    private final int sleepAmount;


    public GeneratorService(@Qualifier("getAccountsPerBank") int accountsPerBank,
                            @Qualifier("getTotalAccounts") int totalAccounts,
                            @Qualifier("getSleepAmount") int sleepAmount) {
        this.accountsPerBank = accountsPerBank;
        this.totalAccounts = totalAccounts;
        this.sleepAmount = sleepAmount;
        accounts = new long[totalAccounts][2];
        populateAccounts();
    }

    @PostConstruct
    public void init() {
        log.info("Accounts Per Bank: " + accountsPerBank + "\n");
        log.info("Total Accounts: " + totalAccounts + "\n");
        log.info("Sleep Amount: " + sleepAmount + "\n");

    }


    long[][] accounts;
    final ObjectMapper objectMapper = new ObjectMapper();
    final Gson gson = new Gson();
    final RestTemplate restTemplate = new RestTemplate();







    public void generate() throws InterruptedException {
        int i = 0, j  = 5;
        int len = totalAccounts;
        while (true) {
            i = i % len;
            j = j % len;

            Creditor creditor = new Creditor(accounts[i][0], accounts[i][1]);
            Debtor debtor = new Debtor(accounts[j][0], accounts[j][1]);
            LocalDate date = LocalDate.now();
            BigDecimal amount = getRandomAmount();
            String memo = debtor + " " + creditor + " " + amount + date;
            TransferMessage transferMessage = TransferMessage.builder()
                    .creditor(creditor)
                    .debtor(debtor)
                    .amount(amount)
                    .date(date)
                    .memo(memo)
                    .build();
            tech.nermindedovic.library.avro.TransferMessage message = new tech.nermindedovic.library.avro.TransferMessage(
                    transferMessage.getMessageId(),
                    new tech.nermindedovic.library.avro.Creditor(creditor.getAccountNumber(), creditor.getRoutingNumber()),
                    new tech.nermindedovic.library.avro.Debtor(debtor.getAccountNumber(), debtor.getRoutingNumber()),
                    date.toString(),
                    amount.toString(),
                    memo
            );


            HttpEntity<String> request;
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            Thread.sleep(sleepAmount);


            //                request = new HttpEntity<>(objectMapper.writeValueAsString(transferMessage), headers);
//            request = new HttpEntity<>(objectMapper.writeValueAsString(message), headers);
            request = new HttpEntity<>(gson.toJson(message), headers);
            log.info(request.toString());
            restTemplate.postForLocation("http://localhost:8080/funds/transfer", request);


            i++;j++;
        }
    }

    private void populateAccounts() {
        int userId = 1;
        int i = 0;
        int j = accountsPerBank;
        while (i < accountsPerBank) {
            accounts[i] = new long[] {userId, BANK_A_ROUTING};
            accounts[j] = new long[] {userId, BANK_B_ROUTING};
            i++;j++;
            userId = userId % accountsPerBank + 1;
        }
    }




    private BigDecimal getRandomAmount() {
        DecimalFormat df = new DecimalFormat(".##");
        String ofBD = df.format(ThreadLocalRandom.current().nextDouble(6400));
        return new BigDecimal(ofBD);
    }



}