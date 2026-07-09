package com.domoticore.maintenance.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.ResourceAuditMetadata;
import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceService {

    private static final String COLLECTION = "maintenance-records";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;
    private final ObjectMapper objectMapper;

    public MaintenanceService(UserScopedJsonResourceService scopedJsonResourceService, ObjectMapper objectMapper) {
        this.scopedJsonResourceService = scopedJsonResourceService;
        this.objectMapper = objectMapper;
    }

    public ArrayNode listRecords(Long userId) {
        JsonNode state = scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
        JsonNode records = state.get("records");
        if (records instanceof ArrayNode arrayNode) {
            return arrayNode;
        }
        return objectMapper.createArrayNode();
    }

    @Transactional
    public JsonNode registerRecord(User user, JsonNode payload) {
        String deviceName = payload.path("deviceName").asText("").trim();
        String description = payload.path("description").asText("").trim();
        String performedAt = payload.path("performedAt").asText("").trim();

        if (deviceName.isBlank() || description.isBlank() || performedAt.isBlank()) {
            throw new IllegalArgumentException("maintenance.error.requiredFields");
        }

        ObjectNode record = objectMapper.createObjectNode();
        record.put("id", "maint-" + System.currentTimeMillis());
        record.put("deviceId", payload.path("deviceId").asText(
                deviceName.toLowerCase().replaceAll("\\s+", "-")));
        record.put("deviceName", deviceName);
        record.put("performedAt", performedAt);
        record.put("description", description);
        if (payload.hasNonNull("technician") && !payload.path("technician").asText("").isBlank()) {
            record.put("technician", payload.path("technician").asText());
        }
        ResourceAuditMetadata.stampCreated(record, user);

        ArrayNode records = listRecords(user.getId());
        ArrayNode updated = objectMapper.createArrayNode();
        updated.add(record);
        records.forEach(updated::add);

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("records", updated);
        scopedJsonResourceService.patchFromTemplate(COLLECTION, user.getId(), TEMPLATE_ID, patch);
        return record;
    }
}
