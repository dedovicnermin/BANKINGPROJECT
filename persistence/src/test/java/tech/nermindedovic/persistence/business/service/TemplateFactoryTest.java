package tech.nermindedovic.persistence.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.library.pojos.TransferValidation;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemplateFactoryTest {

    @Mock
    KafkaTemplate<String, String> stringTemplate;
    @Mock
    KafkaTemplate<String, TransferValidation> validationTemplate;
    @Mock
    KafkaTemplate<String, TransferStatus> statusTemplate;

    @Test
    void willRetrieveStringTemplate() {
        TemplateFactory factory = new TemplateFactory(stringTemplate, validationTemplate, statusTemplate);
        assertEquals(stringTemplate, factory.getStringTemplate());
    }

    @Test
    void willRetrieveValidationTemplate() {
        TemplateFactory factory = new TemplateFactory(stringTemplate, validationTemplate, statusTemplate);
        assertEquals(validationTemplate, factory.getValidationTemplate());
    }

    @Test
    void willRetrieveStatusTemplate() {
        TemplateFactory factory = new TemplateFactory(stringTemplate, validationTemplate, statusTemplate);
        assertEquals(statusTemplate, factory.getStatusTemplate());
    }

}