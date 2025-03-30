package io.geraldaddo.hc.doctors_service.configurations;

import io.geraldaddo.hc.cache_module.CacheModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CacheModule.class)
public class CacheConfiguration {
}
