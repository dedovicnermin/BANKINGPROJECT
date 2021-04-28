package tech.nermindedovic.rest.business.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"messageId", "creditor", "debtor", "date", "amount", "memo"})
public class TransferMessage {


    @JsonProperty("messageId")
    private long messageId;

    @Valid
    @NotNull
    @JsonProperty(required = true)
    private Creditor creditor;

    @Valid
    @NotNull
    @JsonProperty(required = true)
    private Debtor debtor;

    @JsonProperty(required = true)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate date;



    @Positive
    private BigDecimal amount;


    private String memo;







}
