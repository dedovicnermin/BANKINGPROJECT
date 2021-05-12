package tech.nermindedovic.rest.api.elastic;

import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;


public class BankTransaction {

    @Id
    private String id;

    @Field(type = FieldType.Double, name = "amount")
    private Double amount;

    @Field(type = FieldType.Long, name = "creditor_account")
    private Long creditorAccountNumber;

    @Field(type = FieldType.Long, name = "debtor_account")
    private Long debtorAccountNumber;

    @Field(type = FieldType.Date, name = "date", format = DateFormat.basic_date)
    private LocalDate date;

    @Field(type = FieldType.Text, name = "memo")
    private String memo;


}
