package io.geraldaddo.hc.doctors_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class DoctorsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoctorsServiceApplication.class, args);
    }
}