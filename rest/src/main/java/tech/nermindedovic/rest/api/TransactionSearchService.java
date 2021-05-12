package tech.nermindedovic.rest.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import tech.nermindedovic.rest.api.elastic.BankTransaction;

@Service
@Slf4j
public class TransactionSearchService {
    private static final String BANK_INDEX = "postgres.bank.transactions";
    private static final String BANK2_INDEX = "postgres.bank2.transactions";


    private final ElasticsearchOperations elasticsearchOperations;

    public TransactionSearchService(final ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    private final Criteria criteria = new Criteria("transaction_id").exists();
    private final Query searchQuery = new CriteriaQuery(criteria);

    public SearchHits<BankTransaction> retrieveAllTransactions() {
        return elasticsearchOperations.search(searchQuery, BankTransaction.class, IndexCoordinates.of(BANK_INDEX, BANK2_INDEX));
    }

    public SearchHits<BankTransaction> retrieveAllFromBank1() {
        return elasticsearchOperations.search(searchQuery, BankTransaction.class, IndexCoordinates.of(BANK_INDEX));
    }

    public SearchHits<BankTransaction> retrieveAllFromBank2() {
        return elasticsearchOperations.search(searchQuery, BankTransaction.class, IndexCoordinates.of(BANK2_INDEX));
    }







}
