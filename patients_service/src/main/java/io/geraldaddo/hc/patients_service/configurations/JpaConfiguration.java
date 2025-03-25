package io.geraldaddo.hc.patients_service.configurations;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("io.geraldaddo.hc.user_data_module.repositories")
@EntityScan("io.geraldaddo.hc.user_data_module.entities")
public class JpaConfiguration {
}
