package io.geraldaddo.hc.notification_service.components;

import io.geraldaddo.hc.kafka_config_module.models.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AppointmentConsumer implements KafkaConsumer {
    Logger logger = LogManager.getLogger(AppointmentConsumer.class);

    @Override
    @KafkaListener(topics = "appointment", groupId = "notifications")
    public void consume(String message) {
        logger.info("Got message from appointment consumer");
        logger.info(message);
    }
}
