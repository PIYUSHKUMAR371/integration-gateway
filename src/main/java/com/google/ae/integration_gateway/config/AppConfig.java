package com.google.ae.integration_gateway.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    /**
     * Modern Spring Boot 3 RestClient Bean for making outbound enterprise integration calls.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    /**
     * Custom Jackson ObjectMapper configured to ignore unknown properties 
     * from third-party vendor JSON payloads gracefully.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}