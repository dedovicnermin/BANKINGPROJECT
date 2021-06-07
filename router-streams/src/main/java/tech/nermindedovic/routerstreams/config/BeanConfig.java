package tech.nermindedovic.routerstreams.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.jdom2.input.SAXBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;

@Configuration
public class BeanConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SAXBuilder saxBuilder() {
        return new SAXBuilder();
    }


    @Bean
    public Serde<TransferValidation> validationSerde() {
        return new CustomSerdes.TransferValidationSerde();
    }

    @Bean
    public Serde<PaymentData> paymentDataSerde() {
        return new CustomSerdes.PaymentDataSerde();
    }

    @Bean
    public Serde<TransferStatus> transferStatusSerde() {
        return new CustomSerdes.TransferStatusSerde();
    }


}
