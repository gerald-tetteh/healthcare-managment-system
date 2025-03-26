package io.geraldaddo.hc.patients_service;

import io.geraldaddo.hc.security_module.SecurityModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({SecurityModule.class})
public class PatientsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientsServiceApplication.class, args);
    }
}