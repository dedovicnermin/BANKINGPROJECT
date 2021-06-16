package tech.nermindedovic.rest.api.elastic;



import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/elastic")
public class ElasticAPI {

    private final ElasticService service;
    public ElasticAPI(final ElasticService service) {
        this.service = service;
    }


    /**
     * Limit set at 50.
     * @return List of transactions stored in elasticsearch
     */
    @GetMapping(value = "transactions")
    public List<BankTransaction> getAllTransactions() {
        return service.retrieveAllTransactions();
    }




    /**
     * Gets the transaction by id, else returns transaction not found.
     * @param id transactionId
     * @return string form of BankTransaction
     */
    @GetMapping("/transactions/{id}")
    public String getSpecificTransaction(@PathVariable final String id) {
        return service.getSpecificTransaction(id);
    }




    /**
     * Gets the transaction with the max amount of money transferred.
     * @return string response
     */
    @GetMapping("/transactions/max")
    public String getTransactionWithMaxAmount() {
        BankTransaction highestTransaction = service.getHighestTransaction();
        return highestTransaction == null ? "No transactions have been persisted yet." : highestTransaction.toString();
    }




    /**
     * Gets the transaction with the least amount of money transferred.
     * @return string response
     */
    @GetMapping("/transactions/min")
    public String getTransactionWithMinAmount() {
        BankTransaction lowestTransaction = service.getLowestTransaction();
        return lowestTransaction == null ? "No transactions have been persisted yet." : lowestTransaction.toString();
    }




    /**
     * Gets list of transactions with amount greater than input amount.
     * limit 50.
     * @param amount transaction amount
     * @return list of transactions
     */
    @GetMapping("/transactions/gt")
    public List<BankTransaction> transactionsWithAmountGT (@RequestParam("data") String amount) {
        if (!StringUtils.isNumeric(amount)) return Collections.emptyList();
        BigDecimal bigDecimal = new BigDecimal(amount).setScale(2, RoundingMode.HALF_EVEN);
        return service.getTransactionsWithAmountGT(bigDecimal);
    }




    /**
     * Gets list of transactions with amount less than input amount.
     * limit 50.
     * @param amount transaction amount
     * @return list of transactions
     */
    @GetMapping("/transactions/lt")
    public List<BankTransaction> transactionsWithAmountLT(@RequestParam("data") String amount) {
        BigDecimal bigDecimal = new BigDecimal(amount).setScale(2, RoundingMode.HALF_EVEN);
        return service.getTransactionsWithAmountLT(bigDecimal);
    }






}
