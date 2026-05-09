package com.tawseela.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Flyway clean + migrate when profile {@code schema-reset} is active (not with {@code prod}). */
@Configuration
@Profile("schema-reset & !prod")
public class FlywaySchemaResetConfiguration {

    @Bean
    public FlywayMigrationStrategy flywayCleanThenMigrate() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
