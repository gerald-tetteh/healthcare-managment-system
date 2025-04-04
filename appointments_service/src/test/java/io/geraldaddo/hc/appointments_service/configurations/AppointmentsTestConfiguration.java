package io.geraldaddo.hc.appointments_service.configurations;

import io.geraldaddo.hc.appointments_service.services.AppointmentsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AppointmentsTestConfiguration {
    @Bean
    AppointmentsService appointmentsService() {
        return new AppointmentsService();
    }
}
