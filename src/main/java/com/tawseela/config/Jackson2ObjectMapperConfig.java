package com.tawseela.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Jackson2ObjectMapperConfig {

    /** Jackson 2 API; Boot 4’s default JSON stack is Jackson 3 and does not register this type. */
    @Bean
    public ObjectMapper jackson2ObjectMapper() {
        return new ObjectMapper();
    }
}
