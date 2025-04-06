package io.geraldaddo.hc.appointments_service.web_clients;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClients {

    @Bean(value = "doctors-service")
    WebClient doctorsWebClient() {
        return WebClient.builder()
                .baseUrl("http://doctors-service:8082")
                .build();
    }
}
