package io.geraldaddo.hc.appointments_service.configurations;

import io.geraldaddo.hc.kafka_config_module.KafkaConfigModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaConfigModule.class)
public class KafkaModuleConfiguration {
}
