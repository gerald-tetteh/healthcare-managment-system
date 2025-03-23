package io.geraldaddo.hc.gateway.routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RequestPredicates.POST;

@Configuration
public class GatewayRouter {

    @Bean
    public RouterFunction<ServerResponse> appRoutes() {
        return RouterFunctions
                .route(GET("/patients/**"),http("http://patients-service:8081"))
                .andRoute(POST("/patients/**"),http("http://patients-service:8081"));
    }
}
