package tech.nermindedovic.routerstreams.business.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @JsonProperty("accountNumber")
    public long accountNumber;

    @JsonProperty("routingNumber")
    public long routingNumber;
}
