package tech.nermindedovic.transformer_streams.pojos;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


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


    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate date;



    private BigDecimal amount;

    private String memo;







}
