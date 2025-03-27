package io.geraldaddo.hc.gateway.configurations;

import io.geraldaddo.hc.user_data_module.UserDataModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({UserDataModule.class})
public class UserDataConfiguration {
}
