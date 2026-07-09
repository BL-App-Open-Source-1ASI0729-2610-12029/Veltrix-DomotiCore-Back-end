package com.domoticore.integrations.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BusinessProfileService {

    private static final String COLLECTION = "business-profile";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;
    private final ObjectMapper objectMapper;

    public BusinessProfileService(
            UserScopedJsonResourceService scopedJsonResourceService,
            ObjectMapper objectMapper) {
        this.scopedJsonResourceService = scopedJsonResourceService;
        this.objectMapper = objectMapper;
    }

    public JsonNode getProfile(Long userId) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
    }

    @Transactional
    public JsonNode updateProfile(Long userId, JsonNode patch) {
        return scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
    }

    @Transactional
    public JsonNode regenerateApiKey(Long userId) {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("apiKey", "dc_live_" + UUID.randomUUID().toString().replace("-", ""));
        return updateProfile(userId, patch);
    }
}
