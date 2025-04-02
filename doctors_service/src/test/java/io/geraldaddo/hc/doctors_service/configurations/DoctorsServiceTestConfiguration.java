package io.geraldaddo.hc.doctors_service.configurations;

import io.geraldaddo.hc.doctors_service.services.DoctorsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class DoctorsServiceTestConfiguration {

    @Bean
    DoctorsService doctorsService() {
        return new DoctorsService();
    }
}
