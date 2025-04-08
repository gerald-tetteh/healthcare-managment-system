package io.geraldaddo.hc.kafka_config_module;

import io.geraldaddo.hc.kafka_config_module.configuration.KafkaProducerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaProducerConfig.class)
public class KafkaConfigModule {
}