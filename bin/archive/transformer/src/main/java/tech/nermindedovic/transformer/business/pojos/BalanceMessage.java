package tech.nermindedovic.transformer.business.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonPropertyOrder(value = {"accountNumber", "routingNumber", "balance", "errors"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceMessage {

    @JsonProperty(required = true)
    @XmlElement(required = true)
    private long accountNumber;

    @JsonProperty(required = true)
    private long routingNumber;


    private String balance;
    private boolean errors;


    public boolean getErrors() {
        return errors;
    }
}
