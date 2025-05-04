package io.geraldaddo.hc.notification_service.configuration;

import io.geraldaddo.hc.kafka_config_module.configuration.KafkaConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaConsumerConfig.class)
public class KafkaModuleConfiguration {
}
