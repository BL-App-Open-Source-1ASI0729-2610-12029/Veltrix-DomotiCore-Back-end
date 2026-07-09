package com.domoticore.shared.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Set;

public final class ActivityActorMetadata {

    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String USER_EMAIL = "userEmail";

    private static final Set<String> PROTECTED_FIELDS = Set.of(USER_ID, USER_NAME, USER_EMAIL);

    private ActivityActorMetadata() {
    }

    public static void stampActor(ObjectNode payload, User user) {
        stripActorFields(payload);
        payload.put(USER_ID, user.getId());
        payload.put(USER_NAME, user.getName());
        payload.put(USER_EMAIL, user.getEmail());
    }

    public static void stampSystemSeed(ObjectNode payload, long userId, String userName, String userEmail) {
        stripActorFields(payload);
        payload.put(USER_ID, userId);
        payload.put(USER_NAME, userName);
        payload.put(USER_EMAIL, userEmail);
    }

    public static void stripActorFields(ObjectNode payload) {
        PROTECTED_FIELDS.forEach(payload::remove);
    }

    public static ObjectNode sanitizePatch(ObjectNode patch) {
        ObjectNode sanitized = patch.deepCopy();
        stripActorFields(sanitized);
        return sanitized;
    }

    public static boolean belongsToUser(JsonNode entry, Long userId) {
        if (entry == null || userId == null) {
            return false;
        }
        if (entry.hasNonNull(USER_ID) && entry.get(USER_ID).asLong() == userId) {
            return true;
        }
        return entry.hasNonNull(ResourceAuditMetadata.CREATED_BY_USER_ID)
                && entry.get(ResourceAuditMetadata.CREATED_BY_USER_ID).asLong() == userId;
    }
}
