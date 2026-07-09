package com.domoticore.shared.infrastructure.config;

import com.domoticore.shared.infrastructure.security.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtProperties jwtProperties(
            @Value("${domoticore.jwt.secret}") String secret,
            @Value("${domoticore.jwt.expiration-ms}") long expirationMs) {
        return new JwtProperties(secret, expirationMs);
    }
}
