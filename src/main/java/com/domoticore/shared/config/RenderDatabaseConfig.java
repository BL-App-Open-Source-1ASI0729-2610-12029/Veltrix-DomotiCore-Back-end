package com.domoticore.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class RenderDatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(RenderDatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String databaseUrl = env.getProperty("DATABASE_URL");
        String externalDatabaseUrl = env.getProperty("DATABASE_EXTERNAL_URL");
        String host = env.getProperty("DB_HOST");
        String port = env.getProperty("DB_PORT");
        String databaseName = env.getProperty("DB_NAME");
        String username = firstNonBlank(
                env.getProperty("DATABASE_USERNAME"),
                env.getProperty("DATABASE_USER"));
        String password = env.getProperty("DATABASE_PASSWORD");

        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                databaseUrl,
                externalDatabaseUrl,
                host,
                port,
                databaseName,
                username,
                password);

        log.info("Connecting to PostgreSQL host={}", RenderDatabaseUrlConverter.hostForLogging(details.jdbcUrl()));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(details.jdbcUrl());
        config.setUsername(details.username());
        config.setPassword(details.password());
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(60_000);
        return new HikariDataSource(config);
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
