package com.aigym;

import com.aigym.config.AppJwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableConfigurationProperties(AppJwtProperties.class)
@EnableJpaRepositories(
        basePackages = "com.aigym.repository",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.aigym.repository.mongo.*")
)
@EnableMongoRepositories(basePackages = "com.aigym.repository.mongo")
public class AiGymApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiGymApplication.class, args);
	}

}
