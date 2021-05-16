package tech.nermindedovic.generator;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;


@SpringBootApplication
@Slf4j
public class GeneratorApplication implements CommandLineRunner {
    static final long BANK_A_ROUTING = 111, BANK_B_ROUTING = 222;

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }


    static final int accountsPerBank = 15;
    long[][] accounts = new long[accountsPerBank*2][2];
    final ObjectMapper objectMapper = new ObjectMapper();
    final RestTemplate restTemplate = new RestTemplate();


    @Override
    public void run(String... args) throws InterruptedException {
        populateAccounts();
        generate();
    }


    private void generate() throws InterruptedException {
        int i = 0, j  = 5;
        int len = accounts.length;
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


            HttpEntity<String> request;
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            Thread.sleep(800);

            try {
                request = new HttpEntity<>(objectMapper.writeValueAsString(transferMessage), headers);
                log.info(request.toString());
                restTemplate.postForLocation("http://localhost:8080/funds/transfer", request);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                continue;
            }


            i++;j++;
        }
    }

    public long[][] populateAccounts() {
        int userId = 1;
        int i = 0;
        int j = accountsPerBank;
        while (i < accountsPerBank) {
            accounts[i] = new long[] {userId, BANK_A_ROUTING};
            accounts[j] = new long[] {userId, BANK_B_ROUTING};
            i++;j++;
            userId = userId % accountsPerBank + 1;

        }
        return accounts;
    }




    public BigDecimal getRandomAmount() {
        DecimalFormat df = new DecimalFormat(".##");
        String ofBD = df.format(ThreadLocalRandom.current().nextDouble(6400));
        return new BigDecimal(ofBD);
    }



}
