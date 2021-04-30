package tech.nermindedovic.routerstreams.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import tech.nermindedovic.routerstreams.business.domain.TransferValidation;

@Component
public class RouterJsonMapper {

    private final ObjectMapper mapper;
    public RouterJsonMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
    }


    public String toJsonString(final TransferValidation transferValidation) {
        try {
            return mapper.writeValueAsString(transferValidation);
        } catch (JsonProcessingException e) {
            return transferValidation.toString();
        }
    }

}
