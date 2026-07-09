package com.domoticore.integrations.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.infrastructure.persistence.jpa.UserRepository;
import com.domoticore.shared.domain.model.UnauthorizedException;
import com.domoticore.shared.infrastructure.JsonResourceEntity;
import com.domoticore.shared.infrastructure.JsonResourceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeveloperApiKeyService {

    private static final String BUSINESS_PROFILE_COLLECTION = "business-profile";

    private final JsonResourceRepository jsonResourceRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DeveloperApiKeyService(
            JsonResourceRepository jsonResourceRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.jsonResourceRepository = jsonResourceRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public User resolveUserByApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new UnauthorizedException("developer.api.error.missingToken");
        }

        List<JsonResourceEntity> profiles =
                jsonResourceRepository.findByCollectionNameOrderByResourceIdAsc(BUSINESS_PROFILE_COLLECTION);

        for (JsonResourceEntity entity : profiles) {
            JsonNode payload = parsePayload(entity.getJsonPayload());
            if (!payload.hasNonNull("apiKey")) {
                continue;
            }
            if (!apiKey.equals(payload.get("apiKey").asText())) {
                continue;
            }
            Long userId = parseUserId(entity.getResourceId());
            if (userId == null) {
                continue;
            }
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("developer.api.error.invalidToken"));
        }

        throw new UnauthorizedException("developer.api.error.invalidToken");
    }

    public boolean isApiKeyToken(String token) {
        return token != null && token.startsWith("dc_live_");
    }

    private JsonNode parsePayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid business profile payload", ex);
        }
    }

    private Long parseUserId(String resourceId) {
        if (resourceId == null || resourceId.isBlank() || "default".equals(resourceId)) {
            return null;
        }
        try {
            return Long.parseLong(resourceId);
        } catch (NumberFormatException ex) {
            int dashIndex = resourceId.indexOf('-');
            if (dashIndex > 0) {
                try {
                    return Long.parseLong(resourceId.substring(0, dashIndex));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
        }
    }

    public Optional<String> extractApiKey(String authorizationHeader, String apiKeyHeader) {
        if (apiKeyHeader != null && !apiKeyHeader.isBlank()) {
            return Optional.of(apiKeyHeader.trim());
        }
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            if (isApiKeyToken(token)) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }
}
