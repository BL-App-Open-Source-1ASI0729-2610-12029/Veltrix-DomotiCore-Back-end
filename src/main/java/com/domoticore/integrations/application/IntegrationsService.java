package com.domoticore.integrations.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationsService {

    private static final String COLLECTION = "smart-integrations";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;
    private final ObjectMapper objectMapper;

    public IntegrationsService(
            UserScopedJsonResourceService scopedJsonResourceService,
            ObjectMapper objectMapper) {
        this.scopedJsonResourceService = scopedJsonResourceService;
        this.objectMapper = objectMapper;
    }

    public JsonNode getIntegrations(Long userId) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
    }

    @Transactional
    public JsonNode updateIntegrations(Long userId, JsonNode patch) {
        return scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
    }

    public JsonNode checkCompatibility(Long userId, String modelOrType) {
        JsonNode integrations = getIntegrations(userId);
        String query = modelOrType == null ? "" : modelOrType.trim().toLowerCase();

        ObjectNode response = objectMapper.createObjectNode();
        response.put("modelOrType", modelOrType);

        if (query.isBlank()) {
            response.put("compatible", false);
            response.put("messageKey", "integrations.compatibility.missingInput");
            return response;
        }

        ArrayNode catalog = integrations.path("compatibilityCatalog").isArray()
                ? (ArrayNode) integrations.get("compatibilityCatalog")
                : objectMapper.createArrayNode();

        for (JsonNode entry : catalog) {
            String model = entry.path("model").asText("").toLowerCase();
            String type = entry.path("type").asText("").toLowerCase();
            if (query.equals(model) || query.equals(type) || model.contains(query) || type.contains(query)) {
                response.put("compatible", entry.path("compatible").asBoolean(false));
                response.put("messageKey", entry.path("compatible").asBoolean(false)
                        ? "integrations.compatibility.supported"
                        : "integrations.compatibility.unsupported");
                response.put("matchedModel", entry.path("model").asText());
                response.put("matchedType", entry.path("type").asText());
                return response;
            }
        }

        response.put("compatible", false);
        response.put("messageKey", "integrations.compatibility.notFound");
        return response;
    }
}
