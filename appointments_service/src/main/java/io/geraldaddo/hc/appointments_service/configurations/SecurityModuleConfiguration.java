package io.geraldaddo.hc.appointments_service.configurations;

import io.geraldaddo.hc.security_module.SecurityModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SecurityModule.class)
public class SecurityModuleConfiguration {
}
