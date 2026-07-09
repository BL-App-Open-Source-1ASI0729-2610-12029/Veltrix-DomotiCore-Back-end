package com.domoticore.shared.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.infrastructure.JsonResourceEntity;
import com.domoticore.shared.infrastructure.JsonResourceRepository;
import com.domoticore.shared.infrastructure.security.UserDataScopeResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserCollectionAccessService {

    private static final Pattern SCOPED_RESOURCE_ID = Pattern.compile("^\\d+-(smart-home|small-business)-.+$");

    private final JsonResourceService jsonResourceService;
    private final JsonResourceRepository repository;
    private final UserDataScopeResolver scopeResolver;
    private final ObjectMapper objectMapper;

    public UserCollectionAccessService(
            JsonResourceService jsonResourceService,
            JsonResourceRepository repository,
            UserDataScopeResolver scopeResolver,
            ObjectMapper objectMapper) {
        this.jsonResourceService = jsonResourceService;
        this.repository = repository;
        this.scopeResolver = scopeResolver;
        this.objectMapper = objectMapper;
    }

    public String resolveSegment(User user, String headerSegment) {
        return scopeResolver.resolveSegment(user, headerSegment);
    }

    public String scopePrefix(Long userId, String segment) {
        return scopeResolver.scopePrefix(userId, segment);
    }

    @Transactional
    public List<JsonNode> list(User user, String segment, String collectionName) {
        ensureSeeded(user.getId(), segment, collectionName);
        String prefix = scopePrefix(user.getId(), segment);
        return repository.findByCollectionNameAndResourceIdStartingWith(collectionName, prefix).stream()
                .map(entity -> toPublicNode(entity, prefix))
                .toList();
    }

    @Transactional
    public JsonNode getById(User user, String segment, String collectionName, String publicId) {
        ensureSeeded(user.getId(), segment, collectionName);
        String scopedId = scopedResourceId(user.getId(), segment, publicId);
        JsonNode node = jsonResourceService.findById(collectionName, scopedId);
        return toPublicNode(node, scopedId, prefix(user.getId(), segment));
    }

    @Transactional
    public JsonNode create(User user, String segment, String collectionName, JsonNode body) {
        ensureSeeded(user.getId(), segment, collectionName);
        String publicId = extractPublicId(body);
        ObjectNode payload = body.deepCopy();
        payload.put("id", scopedResourceId(user.getId(), segment, publicId));
        JsonNode created = jsonResourceService.create(collectionName, payload);
        return toPublicNode(created, scopedResourceId(user.getId(), segment, publicId), prefix(user.getId(), segment));
    }

    @Transactional
    public JsonNode patch(User user, String segment, String collectionName, String publicId, JsonNode body) {
        ensureSeeded(user.getId(), segment, collectionName);
        String scopedId = scopedResourceId(user.getId(), segment, publicId);
        JsonNode patched = jsonResourceService.patch(collectionName, scopedId, body);
        return toPublicNode(patched, scopedId, prefix(user.getId(), segment));
    }

    @Transactional
    public void delete(User user, String segment, String collectionName, String publicId) {
        ensureSeeded(user.getId(), segment, collectionName);
        jsonResourceService.delete(collectionName, scopedResourceId(user.getId(), segment, publicId));
    }

    @Transactional
    public JsonNode getSingleton(User user, String segment, String collectionName, String templateId) {
        return getById(user, segment, collectionName, templateId);
    }

    @Transactional
    public JsonNode toggleBooleanField(
            User user,
            String segment,
            String collectionName,
            String publicId,
            String fieldName) {
        ensureSeeded(user.getId(), segment, collectionName);
        String scopedId = scopedResourceId(user.getId(), segment, publicId);
        JsonNode toggled = jsonResourceService.toggleBooleanField(collectionName, scopedId, fieldName);
        return toPublicNode(toggled, scopedId, prefix(user.getId(), segment));
    }

    public String scopedResourceId(Long userId, String segment, String publicId) {
        return prefix(userId, segment) + publicId;
    }

    @Transactional
    public void ensureSeeded(Long userId, String segment, String collectionName) {
        String scopePrefix = prefix(userId, segment);
        if (repository.existsByCollectionNameAndResourceIdStartingWith(collectionName, scopePrefix)) {
            return;
        }

        for (JsonNode template : jsonResourceService.findAll(collectionName)) {
            String templateId = extractPublicId(template);
            if (!isGlobalTemplate(templateId)) {
                continue;
            }
            ObjectNode copy = template.deepCopy();
            copy.put("id", scopedResourceId(userId, segment, templateId));
            jsonResourceService.create(collectionName, copy);
        }
    }

    private String prefix(Long userId, String segment) {
        return scopeResolver.scopePrefix(userId, segment);
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

    private JsonNode publicIdNode(String publicId) {
        if (publicId.matches("^-?\\d+$")) {
            return JsonNodeFactory.instance.numberNode(Long.parseLong(publicId));
        }
        return JsonNodeFactory.instance.textNode(publicId);
    }

    private JsonNode toPublicNode(JsonResourceEntity entity, String prefix) {
        try {
            JsonNode node = objectMapper.readTree(entity.getJsonPayload());
            return toPublicNode(node, entity.getResourceId(), prefix);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid JSON payload", ex);
        }
    }

    private JsonNode toPublicNode(JsonNode payload, String scopedId, String prefix) {
        if (!(payload instanceof ObjectNode objectNode)) {
            return payload;
        }
        ObjectNode copy = objectNode.deepCopy();
        String publicId = scopedId.substring(prefix.length());
        copy.set("id", publicIdNode(publicId));
        return copy;
    }
}
