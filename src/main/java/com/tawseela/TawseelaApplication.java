package com.tawseela;

import com.tawseela.config.TawseelaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(TawseelaProperties.class)
@EnableScheduling
public class TawseelaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TawseelaApplication.class, args);
    }
}
