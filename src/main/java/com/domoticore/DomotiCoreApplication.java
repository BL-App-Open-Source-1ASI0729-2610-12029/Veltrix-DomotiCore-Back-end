package com.domoticore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.boot.context.properties.EnableConfigurationProperties(
        com.domoticore.shared.infrastructure.config.DomotiCoreMailProperties.class)
public class DomotiCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomotiCoreApplication.class, args);
    }
}
