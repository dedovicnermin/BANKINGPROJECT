package tech.nermindedovic.rest.business.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferMessage {

    @NotNull
    private long message_id;

    @Valid
    private Creditor creditor;

    @Valid
    private Debtor debtor;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date date;


//    @JsonDeserialize(as = NumberDeserializers.BigDecimalDeserializer.class)
    @Positive
    private BigDecimal amount;


    private String memo;







}
