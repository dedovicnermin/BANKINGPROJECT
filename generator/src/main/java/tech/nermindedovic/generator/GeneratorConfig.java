package tech.nermindedovic.generator;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneratorConfig {


    @Value("${accounts.per.bank}")
    private int accountsPerBank;


    @Value("${sleep.amount.ms}")
    private int sleepAmount;


    @Value("${total.accounts}")
    private int totalAccounts;


    @Bean
    public int getAccountsPerBank() {
        return accountsPerBank;
    }

    @Bean
    public int getSleepAmount() {
        return sleepAmount;
    }

    @Bean
    public int getTotalAccounts() {
        return totalAccounts;
    }

}
