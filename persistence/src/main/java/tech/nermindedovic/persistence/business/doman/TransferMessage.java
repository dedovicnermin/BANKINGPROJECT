package tech.nermindedovic.persistence.business.doman;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.text.DateFormat;
import java.util.Date;

public class TransferMessage {

    private long message_id;


    private Creditor creditor;

    private Debtor debtor;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date date;


    private long amount;

    private String memo;





}
