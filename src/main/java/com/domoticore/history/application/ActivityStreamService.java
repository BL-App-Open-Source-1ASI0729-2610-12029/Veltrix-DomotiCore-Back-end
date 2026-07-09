package com.domoticore.history.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.ActivityActorMetadata;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.domain.model.ForbiddenException;
import com.domoticore.shared.infrastructure.security.PlatformRole;
import com.domoticore.shared.infrastructure.security.RolePermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityStreamService {

    private static final String COLLECTION = "activity-streams";

    private final UserCollectionAccessService userCollectionAccessService;
    private final RolePermissionService rolePermissionService;
    private final ObjectMapper objectMapper;

    public ActivityStreamService(
            UserCollectionAccessService userCollectionAccessService,
            RolePermissionService rolePermissionService,
            ObjectMapper objectMapper) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.rolePermissionService = rolePermissionService;
        this.objectMapper = objectMapper;
    }

    public List<JsonNode> list(User user, String segment) {
        List<JsonNode> entries = userCollectionAccessService.list(user, segment, COLLECTION);
        if (isAdmin(user)) {
            return entries;
        }
        return entries.stream()
                .filter(entry -> ActivityActorMetadata.belongsToUser(entry, user.getId()))
                .toList();
    }

    public JsonNode getById(User user, String segment, String id) {
        JsonNode entry = userCollectionAccessService.getById(user, segment, COLLECTION, id);
        assertCanView(user, entry);
        return entry;
    }

    @Transactional
    public JsonNode create(User user, String segment, JsonNode body) {
        ObjectNode payload = body instanceof ObjectNode objectNode
                ? objectNode.deepCopy()
                : objectMapper.convertValue(body, ObjectNode.class);
        ActivityActorMetadata.stripActorFields(payload);
        ActivityActorMetadata.stampActor(payload, user);
        return userCollectionAccessService.create(user, segment, COLLECTION, payload);
    }

    @Transactional
    public JsonNode patch(User user, String segment, String id, JsonNode body) {
        JsonNode existing = userCollectionAccessService.getById(user, segment, COLLECTION, id);
        assertCanManage(user, existing);

        ObjectNode patch = body instanceof ObjectNode objectNode
                ? ActivityActorMetadata.sanitizePatch(objectNode)
                : objectMapper.createObjectNode();
        return userCollectionAccessService.patch(user, segment, COLLECTION, id, patch);
    }

    @Transactional
    public void delete(User user, String segment, String id) {
        JsonNode existing = userCollectionAccessService.getById(user, segment, COLLECTION, id);
        assertCanManage(user, existing);
        userCollectionAccessService.delete(user, segment, COLLECTION, id);
    }

    private void assertCanView(User user, JsonNode entry) {
        if (isAdmin(user) || ActivityActorMetadata.belongsToUser(entry, user.getId())) {
            return;
        }
        throw new ForbiddenException("history.activity.error.forbidden");
    }

    private void assertCanManage(User user, JsonNode entry) {
        if (isAdmin(user) || ActivityActorMetadata.belongsToUser(entry, user.getId())) {
            return;
        }
        throw new ForbiddenException("history.activity.error.forbidden");
    }

    private boolean isAdmin(User user) {
        return rolePermissionService.roleOf(user) == PlatformRole.ADMIN;
    }
}
