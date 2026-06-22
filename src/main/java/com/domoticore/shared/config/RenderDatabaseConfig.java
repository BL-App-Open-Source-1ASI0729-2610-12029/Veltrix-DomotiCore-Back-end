package com.domoticore.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class RenderDatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${DATABASE_EXTERNAL_URL:}") String externalDatabaseUrl,
            @Value("${DATABASE_URL:}") String databaseUrl,
            @Value("${DB_HOST:}") String host,
            @Value("${DB_PORT:}") String port,
            @Value("${DB_NAME:}") String databaseName,
            @Value("${DATABASE_USERNAME:}") String username,
            @Value("${DATABASE_PASSWORD:}") String password) {
        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                externalDatabaseUrl != null && !externalDatabaseUrl.isBlank() ? externalDatabaseUrl : databaseUrl,
                host,
                port,
                databaseName,
                username,
                password);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(details.jdbcUrl());
        config.setUsername(details.username());
        config.setPassword(details.password());
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30_000);
        return new HikariDataSource(config);
    }
}
