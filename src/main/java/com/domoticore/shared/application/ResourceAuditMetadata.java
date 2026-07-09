package com.domoticore.shared.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.Set;

public final class ResourceAuditMetadata {

    public static final String CREATED_BY_USER_ID = "createdByUserId";
    public static final String CREATED_BY_NAME = "createdByName";
    public static final String CREATED_BY_EMAIL = "createdByEmail";
    public static final String CREATED_BY_ROLE = "createdByRole";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_BY_USER_ID = "updatedByUserId";
    public static final String UPDATED_BY_NAME = "updatedByName";
    public static final String UPDATED_BY_EMAIL = "updatedByEmail";
    public static final String UPDATED_BY_ROLE = "updatedByRole";
    public static final String UPDATED_AT = "updatedAt";

    private static final Set<String> PROTECTED_FIELDS = Set.of(
            CREATED_BY_USER_ID,
            CREATED_BY_NAME,
            CREATED_BY_EMAIL,
            CREATED_BY_ROLE,
            CREATED_AT,
            UPDATED_BY_USER_ID,
            UPDATED_BY_NAME,
            UPDATED_BY_EMAIL,
            UPDATED_BY_ROLE,
            UPDATED_AT);

    private ResourceAuditMetadata() {
    }

    public static void stampCreated(ObjectNode payload, User user) {
        stripProtectedFields(payload);
        payload.put(CREATED_BY_USER_ID, user.getId());
        payload.put(CREATED_BY_NAME, user.getName());
        payload.put(CREATED_BY_EMAIL, user.getEmail());
        payload.put(CREATED_BY_ROLE, user.getRole());
        payload.put(CREATED_AT, Instant.now().toString());
    }

    public static void stampUpdated(ObjectNode payload, User user) {
        payload.put(UPDATED_BY_USER_ID, user.getId());
        payload.put(UPDATED_BY_NAME, user.getName());
        payload.put(UPDATED_BY_EMAIL, user.getEmail());
        payload.put(UPDATED_BY_ROLE, user.getRole());
        payload.put(UPDATED_AT, Instant.now().toString());
    }

    public static void stampSystemSeed(ObjectNode payload) {
        stripProtectedFields(payload);
        payload.put(CREATED_BY_NAME, "DomotiCore");
        payload.put(CREATED_BY_ROLE, "System");
        payload.put(CREATED_AT, Instant.now().toString());
    }

    public static void stripProtectedFields(ObjectNode payload) {
        PROTECTED_FIELDS.forEach(payload::remove);
    }

    public static ObjectNode sanitizePatch(ObjectNode patch) {
        ObjectNode sanitized = patch.deepCopy();
        stripProtectedFields(sanitized);
        return sanitized;
    }
}
