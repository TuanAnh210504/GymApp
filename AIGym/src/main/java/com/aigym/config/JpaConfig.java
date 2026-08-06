package com.aigym.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.aigym.repository",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.aigym.repository.mongo.*")
)
public class JpaConfig {
}
