package com.tawseela;

import com.tawseela.config.TawseelaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableConfigurationProperties(TawseelaProperties.class)
@EnableMethodSecurity
@EnableScheduling
public class TawseelaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TawseelaApplication.class, args);
    }
}
