package tech.nermindedovic.transformer.pojos;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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


//    @JsonDeserialize(as = NumberDeserializers.BigDecimalDeserializer.class)
    private BigDecimal amount;

    private String memo;







}
