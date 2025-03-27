package io.geraldaddo.hc.patients_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class PatientsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientsServiceApplication.class, args);
    }
}