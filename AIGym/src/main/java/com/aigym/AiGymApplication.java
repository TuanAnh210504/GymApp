package com.aigym;

import com.aigym.config.AppJwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppJwtProperties.class)
public class AiGymApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiGymApplication.class, args);
	}

}
