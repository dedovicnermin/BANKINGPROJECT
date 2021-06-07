package tech.nermindedovic.routerstreams.config.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.utils.RouterAppUtils;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.util.function.Function;


@Configuration
@EnableAutoConfiguration
@Slf4j
public class ErrorProcessor {

    //  TESTED : 👍🏼


    /**
     * IN: router.trsf.error.handler
     * OUT: funds.transfer.error
     * @return KStream
     */
    @Bean
    public Function<KStream<String, PaymentData>, KStream<String, String>> transferErrorHandler() {
        return input -> {
            KStream<String, String> stream = input
                    .selectKey((key, val) -> getMessageIdOrNull(val))
                    .filter((key, val) -> key != null)
                    .mapValues(paymentData -> RouterAppUtils.TRANSFER_ERROR_PREFIX + paymentData.getTransferMessageXml());
            stream.to(RouterTopicNames.TRANSFER_STATUS_FAILED_HANDLER, RouterAppUtils.producedWithStringSerdes);
            return stream;
        };
    }

    /**
     * IN: router.validation.error.handler
     * OUT: funds.transfer.error
     * @return KStream
     * KEY ALREADY ASSIGNED AT THIS POINT
     */
    @Bean
    public Function<KStream<String, TransferValidation>, KStream<String, String>> validationErrorHandler() {
        return input -> {
            KStream<String, String> stream = input
                    .peek((key, val) -> log.info("validationErrorHandler(48)--KEY:" + key + ", VALUE:" + val))
                    .mapValues(transferValidation -> RouterAppUtils.VALIDATION_ERROR_PREFIX + transferValidation.getTransferMessage());
            stream.to(RouterTopicNames.TRANSFER_STATUS_FAILED_HANDLER, RouterAppUtils.producedWithStringSerdes);
            return stream;
        };
    }


    private String getMessageIdOrNull(PaymentData data) {
        return data.getMessageId() != null ? data.getMessageId().toString() : null;
    }




}
