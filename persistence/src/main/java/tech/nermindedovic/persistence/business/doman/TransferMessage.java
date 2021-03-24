package tech.nermindedovic.persistence.business.doman;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferMessage {

    private long message_id;


    private Creditor creditor;

    private Debtor debtor;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date date;


    private long amount;

    private String memo;







}
