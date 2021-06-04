package tech.nermindedovic.routerstreams.config.processors;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.utils.RouterAppUtils;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.util.function.Function;


@Configuration
public class ErrorProcessor {

    //  TESTED : 👍🏼

    @Bean
    public Function<KStream<String, PaymentData>, KStream<String, String>> transferErrorHandler() {
        return input -> input
                .selectKey((key, val) -> getMessageIdOrNull(val))
                .filter((key, val) -> key != null)
                .mapValues(paymentData -> RouterAppUtils.TRANSFER_ERROR_PREFIX + paymentData.getTransferMessageXml())
                .through(RouterTopicNames.TRANSFER_STATUS_FAILED_HANDLER, RouterAppUtils.producedWithStringSerdes);
    }


    // KEY ALREADY ASSIGNED AT THIS POINT
    @Bean
    public Function<KStream<String, TransferValidation>, KStream<String, String>> validationErrorHandler() {
        return input -> input
                .mapValues(transferValidation -> RouterAppUtils.VALIDATION_ERROR_PREFIX + transferValidation.getTransferMessage())
                .through(RouterTopicNames.TRANSFER_STATUS_FAILED_HANDLER, RouterAppUtils.producedWithStringSerdes);
    }


    private String getMessageIdOrNull(PaymentData data) {
        return data.getMessageId() != null ? data.getMessageId().toString() : null;
    }




}
