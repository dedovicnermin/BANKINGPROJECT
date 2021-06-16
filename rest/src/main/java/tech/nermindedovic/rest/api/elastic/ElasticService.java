package tech.nermindedovic.rest.api.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ElasticService {

    private static final String INDEX_NAME = "postgres.bank.transactions";
    private static final IndexCoordinates BANK_INDEX = IndexCoordinates.of(INDEX_NAME);

    private static final String AMOUNT_FIELD = "amount";


    // == constructor ==
    private final ElasticsearchOperations elasticsearchOperations;
    public ElasticService(final ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // == reused criteria ==
    private final Criteria transactionIdExists = new Criteria("transaction_id");
    private final Query tIdExistsQuery = new CriteriaQuery(transactionIdExists);
    private final Criteria amountCriteria = new Criteria(AMOUNT_FIELD);




    /**
     * Retrieves latest transactions with 50 limit
     * @return list of transactions
     */
    public List<BankTransaction> retrieveAllTransactions() {
        if (!elasticsearchOperations.indexOps(BankTransaction.class).exists()) return Collections.emptyList();

        List<SearchHit<BankTransaction>> hits = elasticsearchOperations.search(tIdExistsQuery, BankTransaction.class, BANK_INDEX)
                .stream()
                .limit(50)
                .collect(Collectors.toList());
        return extractTransactions(hits);
    }




    /**
     * Retrieve transaction by ID
     * @param id transaction id
     * @return toString of transaction
     */
    public String getSpecificTransaction(String id) {
        BankTransaction transaction = elasticsearchOperations.get(id, BankTransaction.class, BANK_INDEX);
        return transaction == null ? "Could not find transaction with id - " + id : transaction.toString();
    }




    /**
     * Returns transaction with max amount
     * @return transaction
     */
    public BankTransaction getHighestTransaction() {
        if (!elasticsearchOperations.indexOps(BANK_INDEX).exists()) return null;

        SearchHits<BankTransaction> search = elasticsearchOperations.search(tIdExistsQuery, BankTransaction.class);
        log.info(search.toString());
        Optional<SearchHit<BankTransaction>> max = search
                .stream()
                .max(Comparator.comparing(a -> a.getContent().getAmount()));

        return max.map(SearchHit::getContent).orElse(null);
    }




    /**
     * Returns transaction with min amount
     * @return transaction
     */
    public BankTransaction getLowestTransaction() {
        if (!elasticsearchOperations.indexOps(BANK_INDEX).exists()) return null;

        Optional<SearchHit<BankTransaction>> min = elasticsearchOperations.search(tIdExistsQuery, BankTransaction.class, BANK_INDEX)
                .stream()
                .min(Comparator.comparing(a -> a.getContent().getAmount()));
        return min.map(SearchHit::getContent).orElse(null);
    }




    /**
     * Returns list of transactions with amount greater than specified amount.
     * @param amount amount
     * @return transactions
     */
    public List<BankTransaction> getTransactionsWithAmountGT(BigDecimal amount) {
        if (!elasticsearchOperations.indexOps(BANK_INDEX).exists()) return Collections.emptyList();

        Criteria greaterThan = amountCriteria.greaterThan(amount);
        CriteriaQuery criteriaQuery = new CriteriaQuery(greaterThan);


        List<SearchHit<BankTransaction>> hits = elasticsearchOperations.search(criteriaQuery, BankTransaction.class)
                .stream()
                .limit(50)
                .collect(Collectors.toList());
        return extractTransactions(hits);
    }




    /**
     * Returns list of transactions with amount lower than specified amount
     * @param amount amount
     * @return transactions
     */
    public List<BankTransaction> getTransactionsWithAmountLT(BigDecimal amount) {
        if (!elasticsearchOperations.indexOps(BANK_INDEX).exists()) return Collections.emptyList();
        Criteria lessThan = new Criteria(AMOUNT_FIELD).exists().lessThanEqual(amount);
        CriteriaQuery criteriaQuery = new CriteriaQuery(lessThan);
        List<SearchHit<BankTransaction>> hits = elasticsearchOperations.search(criteriaQuery, BankTransaction.class)
                .stream()
                .limit(50)
                .collect(Collectors.toList());
        return extractTransactions(hits);

    }




    private List<BankTransaction> extractTransactions(List<SearchHit<BankTransaction>> hits) {
        final List<BankTransaction> transactions = new ArrayList<>();
        for (SearchHit<BankTransaction> hit : hits) {
            transactions.add(hit.getContent());
        }
        return transactions;
    }





}
