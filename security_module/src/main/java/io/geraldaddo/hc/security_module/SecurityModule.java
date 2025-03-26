package io.geraldaddo.hc.security_module;

import io.geraldaddo.hc.security_module.configurations.SecurityConfiguration;
import io.geraldaddo.hc.security_module.configurations.UserAuthorisationFilter;
import io.geraldaddo.hc.security_module.services.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SecurityConfiguration.class, UserAuthorisationFilter.class, JwtService.class})
public class SecurityModule {
}
