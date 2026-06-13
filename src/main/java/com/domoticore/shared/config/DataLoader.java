package com.domoticore.shared.config;

import com.domoticore.iam.infrastructure.UserRepository;
import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.infrastructure.JsonResourceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;

@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private static final String[] JSON_COLLECTIONS = {
            "devices-overview",
            "device-details",
            "activity-streams",
            "history-summary",
            "security-cameras",
            "smart-locks",
            "authorized-users",
            "security-log",
            "notification-feed",
            "history-insights",
            "automation-recipe",
            "automation-builder-triggers",
            "automation-builder-conditions",
            "automation-builder-actions",
            "automation-suggested-templates",
            "user-profile"
    };

    private static final String[] PHASE2_COLLECTIONS = {
            "automation-rules",
            "automation-group-schedules",
            "automation-shutdown-protocol",
            "automation-efficiency-insights",
            "automation-active-rule-timeline",
            "automation-active-scenes",
            "automation-upcoming-events",
            "automation-smart-suggestion",
            "team-management",
            "business-profile",
            "operations-hub-snapshot"
    };

    @Bean
    CommandLineRunner seedDatabase(
            ObjectMapper objectMapper,
            JsonResourceService jsonResourceService,
            JsonResourceRepository jsonResourceRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> seed(objectMapper, jsonResourceService, jsonResourceRepository, userRepository, passwordEncoder);
    }

    static void seed(
            ObjectMapper objectMapper,
            JsonResourceService jsonResourceService,
            JsonResourceRepository jsonResourceRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) throws Exception {
        if (userRepository.count() == 0) {
            seedUsers(objectMapper, userRepository, passwordEncoder);
        }

        if (jsonResourceRepository.count() == 0) {
            seedJsonCollections(objectMapper, jsonResourceService);
            seedPhase2Collections(objectMapper, jsonResourceService);
        } else {
            seedPhase2Collections(objectMapper, jsonResourceService);
        }
    }

    private static void seedUsers(
            ObjectMapper objectMapper,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) throws Exception {
        JsonNode usersNode = loadDbJson(objectMapper).get("users");
        if (usersNode == null || !usersNode.isArray()) {
            return;
        }

        for (JsonNode userNode : usersNode) {
            com.domoticore.iam.domain.User user = new com.domoticore.iam.domain.User();
            user.setName(userNode.get("name").asText());
            user.setEmail(userNode.get("email").asText().toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(userNode.get("password").asText()));
            user.setRole(userNode.path("role").asText("Admin"));
            if (userNode.hasNonNull("avatar")) {
                user.setAvatar(userNode.get("avatar").asText());
            }
            user.setOnboardingCompleted(false);
            userRepository.save(user);
        }
        log.info("Seeded demo users");
    }

    private static void seedJsonCollections(ObjectMapper objectMapper, JsonResourceService jsonResourceService)
            throws Exception {
        JsonNode root = loadDbJson(objectMapper);

        for (String collection : JSON_COLLECTIONS) {
            JsonNode arrayNode = root.get(collection);
            if (arrayNode == null || !arrayNode.isArray()) {
                log.warn("Collection {} not found in db.json", collection);
                continue;
            }

            for (JsonNode item : arrayNode) {
                jsonResourceService.create(collection, item);
            }
            log.info("Seeded collection {}", collection);
        }
    }

    private static void seedPhase2Collections(ObjectMapper objectMapper, JsonResourceService jsonResourceService)
            throws Exception {
        JsonNode root = loadPhase2Json(objectMapper);

        for (String collection : PHASE2_COLLECTIONS) {
            if (jsonResourceService.collectionExists(collection)) {
                continue;
            }

            JsonNode arrayNode = root.get(collection);
            if (arrayNode == null || !arrayNode.isArray()) {
                log.warn("Phase 2 collection {} not found in phase2.json", collection);
                continue;
            }

            for (JsonNode item : arrayNode) {
                jsonResourceService.create(collection, item);
            }
            log.info("Seeded phase 2 collection {}", collection);
        }
    }

    private static JsonNode loadDbJson(ObjectMapper objectMapper) throws Exception {
        ClassPathResource resource = new ClassPathResource("data/db.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);
        }
    }

    private static JsonNode loadPhase2Json(ObjectMapper objectMapper) throws Exception {
        ClassPathResource resource = new ClassPathResource("data/phase2.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);
        }
    }
}
