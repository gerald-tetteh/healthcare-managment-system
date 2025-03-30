package io.geraldaddo.hc.cache_module;

import io.geraldaddo.hc.cache_module.configuration.CacheConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableCaching
@Import(CacheConfiguration.class)
public class CacheModule {

}