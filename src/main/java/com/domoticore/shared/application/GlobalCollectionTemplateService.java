package com.domoticore.shared.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class GlobalCollectionTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalCollectionTemplateService.class);
    private static final Pattern SCOPED_RESOURCE_ID =
            Pattern.compile("^\\d+-(smart-home|small-business)-.+$");

    private static final Map<String, String> COLLECTION_SOURCE = Map.of(
            "devices-overview", "data/db.json",
            "device-details", "data/db.json",
            "business-devices-overview", "data/phase4.json");

    private final JsonResourceService jsonResourceService;
    private final ObjectMapper objectMapper;

    public GlobalCollectionTemplateService(JsonResourceService jsonResourceService, ObjectMapper objectMapper) {
        this.jsonResourceService = jsonResourceService;
        this.objectMapper = objectMapper;
    }

    public void ensureGlobalTemplates(String collectionName) {
        if (hasGlobalTemplate(collectionName)) {
            return;
        }

        String sourcePath = COLLECTION_SOURCE.get(collectionName);
        if (sourcePath == null) {
            LOGGER.warn("No classpath source configured to bootstrap collection {}", collectionName);
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource(sourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(inputStream);
                JsonNode arrayNode = root.get(collectionName);
                if (arrayNode == null || !arrayNode.isArray()) {
                    LOGGER.warn("Collection {} not found in {}", collectionName, sourcePath);
                    return;
                }

                int created = 0;
                for (JsonNode item : arrayNode) {
                    String templateId = extractPublicId(item);
                    if (!isGlobalTemplate(templateId)) {
                        continue;
                    }
                    if (jsonResourceService.exists(collectionName, templateId)) {
                        continue;
                    }
                    jsonResourceService.create(collectionName, item);
                    created++;
                }
                if (created > 0) {
                    LOGGER.info("Bootstrapped {} global template(s) for {}", created, collectionName);
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed to bootstrap global templates for {}: {}", collectionName, ex.getMessage());
        }
    }

    private boolean hasGlobalTemplate(String collectionName) {
        return jsonResourceService.findAll(collectionName).stream()
                .anyMatch(node -> isGlobalTemplate(extractPublicId(node)));
    }

    private boolean isGlobalTemplate(String resourceId) {
        return !SCOPED_RESOURCE_ID.matcher(resourceId).matches();
    }

    private String extractPublicId(JsonNode payload) {
        JsonNode idNode = payload.get("id");
        if (idNode == null || idNode.isNull()) {
            throw new IllegalArgumentException("Payload must include an id field");
        }
        return idNode.isNumber() ? String.valueOf(idNode.numberValue()) : idNode.asText();
    }
}
