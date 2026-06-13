package com.domoticore.shared.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserScopedJsonResourceService {

    private final JsonResourceService jsonResourceService;

    public UserScopedJsonResourceService(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    @Transactional
    public JsonNode getOrCreateFromTemplate(String collectionName, Long userId, String templateId) {
        String scopedId = scopedId(userId);
        if (jsonResourceService.exists(collectionName, scopedId)) {
            return stripInternalId(jsonResourceService.findById(collectionName, scopedId));
        }

        ObjectNode copy = copyTemplate(collectionName, templateId);
        copy.put("id", scopedId);
        return stripInternalId(jsonResourceService.create(collectionName, copy));
    }

    @Transactional
    public JsonNode getOrCreateFromTemplate(String collectionName, Long userId, String scope, String templateId) {
        String scopedId = scopedId(userId, scope);
        if (jsonResourceService.exists(collectionName, scopedId)) {
            return stripInternalId(jsonResourceService.findById(collectionName, scopedId));
        }

        ObjectNode copy = copyTemplate(collectionName, templateId);
        copy.put("id", scopedId);
        return stripInternalId(jsonResourceService.create(collectionName, copy));
    }

    @Transactional
    public JsonNode patchFromTemplate(String collectionName, Long userId, String templateId, JsonNode patch) {
        getOrCreateFromTemplate(collectionName, userId, templateId);
        return stripInternalId(jsonResourceService.patch(collectionName, scopedId(userId), patch));
    }

    @Transactional
    public JsonNode patchFromTemplate(String collectionName, Long userId, String scope, String templateId, JsonNode patch) {
        getOrCreateFromTemplate(collectionName, userId, scope, templateId);
        return stripInternalId(jsonResourceService.patch(collectionName, scopedId(userId, scope), patch));
    }

    private ObjectNode copyTemplate(String collectionName, String templateId) {
        JsonNode template = jsonResourceService.findById(collectionName, templateId);
        if (template instanceof ObjectNode objectNode) {
            return objectNode.deepCopy();
        }
        throw new IllegalArgumentException("Template must be a JSON object: " + collectionName + "/" + templateId);
    }

    private JsonNode stripInternalId(JsonNode payload) {
        if (payload instanceof ObjectNode objectNode) {
            ObjectNode copy = objectNode.deepCopy();
            copy.remove("id");
            return copy;
        }
        return payload;
    }

    private String scopedId(Long userId) {
        return String.valueOf(userId);
    }

    private String scopedId(Long userId, String scope) {
        return userId + "-" + scope;
    }
}
