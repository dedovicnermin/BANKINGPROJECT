package tech.nermindedovic.persistence.business.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.library.pojos.TransferValidation;

@Component
public class TemplateFactory {

    final KafkaTemplate<String, String> stringTemplate;
    final KafkaTemplate<String, TransferValidation> validationTemplate;
    final KafkaTemplate<String, TransferStatus> statusTemplate;

    public TemplateFactory(KafkaTemplate<String, String> stringTemplate, KafkaTemplate<String, TransferValidation> validationTemplate, KafkaTemplate<String, TransferStatus> statusTemplate) {
        this.stringTemplate = stringTemplate;
        this.validationTemplate = validationTemplate;
        this.statusTemplate = statusTemplate;
    }


    public KafkaTemplate<String, String> getStringTemplate() {
        return stringTemplate;
    }

    public KafkaTemplate<String, TransferStatus> getStatusTemplate() {
        return statusTemplate;
    }

    public KafkaTemplate<String, TransferValidation> getValidationTemplate() {return validationTemplate;}




}
