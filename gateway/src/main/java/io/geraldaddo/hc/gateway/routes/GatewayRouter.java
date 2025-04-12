package io.geraldaddo.hc.gateway.routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.*;

@Configuration
public class GatewayRouter {

    @Bean
    public RouterFunction<ServerResponse> appRoutes() {
        return RouterFunctions
                .route(methods(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE).and(path("/patients/**")),
                        http("http://patients-service:8081"))
                .andRoute(methods(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE).and(path("/doctors/**")),
                        http("http://doctors-service:8082"))
                .andRoute(methods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.DELETE)
                                .and(path("/appointments/**")),
                        http("http://appointments-service:8083"))
                .andRoute(methods(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE)
                                .and(path("/medical-records/**")),
                        http("http://medical-records-service:8084"));
    }
}
