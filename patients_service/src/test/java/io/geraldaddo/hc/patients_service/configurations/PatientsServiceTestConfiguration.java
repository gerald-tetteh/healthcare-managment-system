package io.geraldaddo.hc.patients_service.configurations;

import io.geraldaddo.hc.patients_service.services.PatientsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class PatientsServiceTestConfiguration {

    @Bean
    public PatientsService patientsService() {
        return new PatientsService();
    }
}
