package io.geraldaddo.hc.user_data_module;

import io.geraldaddo.hc.user_data_module.attribute_converters.AvailabilityConverter;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("io.geraldaddo.hc.user_data_module.repositories")
@EntityScan("io.geraldaddo.hc.user_data_module.entities")
@Import({AvailabilityConverter.class})
public class UserDataModule {
}
