package com.jyoti.paypulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI payPulseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayPulse — Distributed Payment & Order Processing Engine API")
                        .description("Event-driven microservices architecture featuring Idempotent Payment Processing, Resilience4j Circuit Breakers, and Token Bucket Rate Limiting.")
                        .version("1.0.0")
                        .contact(new Contact().name("Jyoti Pomde").email("jyopomde0001@gmail.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
