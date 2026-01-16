package com.hexamarket.code.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {
	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("API Hexa Market").version("1.0.0")
				.description("This is the API documentation for the Hexa Market application.")
				.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}
}
