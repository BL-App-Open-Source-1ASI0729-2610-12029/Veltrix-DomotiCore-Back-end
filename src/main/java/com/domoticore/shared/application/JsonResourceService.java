package com.domoticore.shared.application;

import com.domoticore.shared.exception.ResourceNotFoundException;
import com.domoticore.shared.infrastructure.JsonResourceEntity;
import com.domoticore.shared.infrastructure.JsonResourceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class JsonResourceService {

    private final JsonResourceRepository repository;
    private final ObjectMapper objectMapper;

    public JsonResourceService(JsonResourceRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public List<JsonNode> findAll(String collectionName) {
        return repository.findByCollectionNameOrderByResourceIdAsc(collectionName).stream()
                .map(this::toJsonNode)
                .toList();
    }

    public JsonNode findById(String collectionName, String resourceId) {
        return repository.findByCollectionNameAndResourceId(collectionName, resourceId)
                .map(this::toJsonNode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found: " + collectionName + "/" + resourceId));
    }

    public JsonNode findSingleton(String collectionName, String resourceId) {
        return findById(collectionName, resourceId);
    }

    public boolean collectionExists(String collectionName) {
        return repository.existsByCollectionName(collectionName);
    }

    @Transactional
    public JsonNode toggleBooleanField(String collectionName, String resourceId, String fieldName) {
        JsonNode current = findById(collectionName, resourceId);
        boolean nextValue = !current.path(fieldName).asBoolean(false);
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put(fieldName, nextValue);
        if (current.has("status")) {
            patch.put("status", nextValue ? "ACTIVE" : "INACTIVE");
        }
        return patch(collectionName, resourceId, patch);
    }

    @Transactional
    public JsonNode create(String collectionName, JsonNode payload) {
        String resourceId = extractId(payload);
        if (repository.findByCollectionNameAndResourceId(collectionName, resourceId).isPresent()) {
            throw new IllegalArgumentException("Resource already exists: " + resourceId);
        }
        return save(collectionName, resourceId, payload);
    }

    @Transactional
    public JsonNode patch(String collectionName, String resourceId, JsonNode patch) {
        JsonResourceEntity entity = repository.findByCollectionNameAndResourceId(collectionName, resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found: " + collectionName + "/" + resourceId));

        ObjectNode existing = (ObjectNode) toJsonNode(entity);
        merge(existing, patch);
        existing.put("id", coerceId(existing.get("id"), resourceId));

        return save(collectionName, resourceId, existing);
    }

    @Transactional
    public void delete(String collectionName, String resourceId) {
        if (repository.findByCollectionNameAndResourceId(collectionName, resourceId).isEmpty()) {
            throw new ResourceNotFoundException("Resource not found: " + collectionName + "/" + resourceId);
        }
        repository.deleteByCollectionNameAndResourceId(collectionName, resourceId);
    }

    @Transactional
    public void seedCollection(String collectionName, List<JsonNode> items) {
        if (repository.existsByCollectionName(collectionName)) {
            return;
        }
        for (JsonNode item : items) {
            String resourceId = extractId(item);
            save(collectionName, resourceId, item);
        }
    }

    private JsonNode save(String collectionName, String resourceId, JsonNode payload) {
        JsonResourceEntity entity = repository
                .findByCollectionNameAndResourceId(collectionName, resourceId)
                .orElseGet(JsonResourceEntity::new);

        entity.setCollectionName(collectionName);
        entity.setResourceId(resourceId);
        entity.setJsonPayload(writeJson(payload));

        return toJsonNode(repository.save(entity));
    }

    private JsonNode toJsonNode(JsonResourceEntity entity) {
        try {
            return objectMapper.readTree(entity.getJsonPayload());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid JSON stored for " + entity.getCollectionName(), ex);
        }
    }

    private String extractId(JsonNode payload) {
        JsonNode idNode = payload.get("id");
        if (idNode == null || idNode.isNull()) {
            throw new IllegalArgumentException("Payload must include an id field");
        }
        return idNode.isNumber() ? String.valueOf(idNode.numberValue()) : idNode.asText();
    }

    private void merge(ObjectNode target, JsonNode patch) {
        Iterator<Map.Entry<String, JsonNode>> fields = patch.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode patchValue = entry.getValue();

            if (patchValue.isObject() && target.has(fieldName) && target.get(fieldName).isObject()) {
                merge((ObjectNode) target.get(fieldName), patchValue);
            } else {
                target.set(fieldName, patchValue);
            }
        }
    }

    private JsonNode coerceId(JsonNode idNode, String fallback) {
        if (idNode == null || idNode.isNull()) {
            return objectMapper.getNodeFactory().textNode(fallback);
        }
        return idNode;
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize JSON payload", ex);
        }
    }
}
