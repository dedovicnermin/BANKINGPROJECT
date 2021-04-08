package tech.nermindedovic.transformer.pojos;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferMessage {

    @JsonProperty(value = "messageId", index = 0)
    private long message_id;


    @JsonProperty(required = true)
    private Creditor creditor;

    @JsonProperty(required = true)
    private Debtor debtor;

    @JsonFormat( pattern = "dd-MM-yyyy hh:mm:ss", lenient = OptBoolean.FALSE, timezone = "America/Chicago")
    private Date date;



    private BigDecimal amount;

    private String memo;







}
