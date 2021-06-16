package tech.nermindedovic.rest.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Configuration
public class BankingTopicsConfig {


    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${banking.partitions:3}")
    private int partitions;

    @Value("${banking.replicas:2}")
    private int replicas;


    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        if (!bootstrapServers.equals("localhost:9099")) {
            configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        }
        configs.put(AdminClientConfig.RETRIES_CONFIG, 5);
        configs.put(AdminClientConfig.CLIENT_ID_CONFIG, "restKafkaAdmin");


        return new KafkaAdmin(configs);
    }

    @Bean
    public KafkaAutoConfiguration kafkaAutoConfiguration() {
        KafkaProperties properties = new KafkaProperties();
        properties.setBootstrapServers(Collections.singletonList(bootstrapServers));
        properties.setClientId("restKafkaAdmin");
        return new KafkaAutoConfiguration(properties);
    }





    @Bean
    public NewTopic balanceTransformerRequest() {
        return TopicBuilder.name("balance.transformer.request")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic balanceTransformerResponse() {
        return TopicBuilder.name("balance.transformer.response")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }


    @Bean
    public NewTopic balanceUpdateRequest() {
        return TopicBuilder.name("balance.update.request")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic balanceUpdateRequestTo111() {
        return TopicBuilder.name("balance.update.request.111")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic balanceUpdateRequestTo222() {
        return TopicBuilder.name("balance.update.request.222")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }


    @Bean
    public NewTopic balanceUpdateResponse() {
        return TopicBuilder.name("balance.update.response")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }


    @Bean
    public NewTopic fundsTransferTo111() {
        return TopicBuilder.name("funds.transfer.111")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic fundsTransferTo222() {
        return TopicBuilder.name("funds.transfer.222")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic fundsTransferError() {
        return TopicBuilder.name("funds.transfer.error")
                .partitions(1)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic fundsTransferRequest() {
        return TopicBuilder.name("funds.transfer.request")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic fundsTransferSingleTo111() {
        return TopicBuilder.name("funds.transfer.single.111")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic fundsTransferSingleTo222() {
        return TopicBuilder.name("funds.transfer.single.222")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic fundsTransferStatus() {
        return TopicBuilder.name("funds.transfer.status")
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }


    @Bean
    public NewTopic fundsTransformerRequest() {
        return TopicBuilder.name("funds.transformer.request")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }


    @Bean
    public NewTopic fundsValidateTo111() {
        return TopicBuilder.name("funds.validate.111")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }


    @Bean
    public NewTopic fundsValidateTo222() {
        return TopicBuilder.name("funds.validate.222")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }


    @Bean
    public NewTopic fundsValidateTransfer() {
        return TopicBuilder.name("router.validate.transfer")
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }






}

