package com.domoticore.integrations.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.domoticore.shared.exception.ResourceNotFoundException;
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

    @Transactional
    public JsonNode createSchedule(Long userId, JsonNode schedule) {
        ObjectNode integrations = readIntegrationsObject(userId);
        ArrayNode schedules = schedulesArray(integrations);
        ObjectNode entry = schedule.deepCopy();
        if (!entry.hasNonNull("id")) {
            entry.put("id", "sched-" + System.currentTimeMillis());
        }
        schedules.add(entry);
        integrations.set("schedules", schedules);
        return saveIntegrations(userId, integrations);
    }

    @Transactional
    public JsonNode updateSchedule(Long userId, String scheduleId, JsonNode patch) {
        ObjectNode integrations = readIntegrationsObject(userId);
        ArrayNode schedules = schedulesArray(integrations);
        for (int i = 0; i < schedules.size(); i++) {
            JsonNode current = schedules.get(i);
            if (!scheduleId.equals(current.path("id").asText())) {
                continue;
            }
            ObjectNode merged = current.deepCopy();
            patch.fields().forEachRemaining(field -> merged.set(field.getKey(), field.getValue()));
            schedules.set(i, merged);
            integrations.set("schedules", schedules);
            JsonNode saved = saveIntegrations(userId, integrations);
            return findSchedule(saved, scheduleId);
        }
        throw new ResourceNotFoundException("integrations.schedule.error.notFound:" + scheduleId);
    }

    @Transactional
    public JsonNode deleteSchedule(Long userId, String scheduleId) {
        ObjectNode integrations = readIntegrationsObject(userId);
        ArrayNode schedules = schedulesArray(integrations);
        ArrayNode updated = objectMapper.createArrayNode();
        boolean removed = false;
        for (JsonNode current : schedules) {
            if (scheduleId.equals(current.path("id").asText())) {
                removed = true;
                continue;
            }
            updated.add(current);
        }
        if (!removed) {
            throw new ResourceNotFoundException("integrations.schedule.error.notFound:" + scheduleId);
        }
        integrations.set("schedules", updated);
        return saveIntegrations(userId, integrations);
    }

    private ObjectNode readIntegrationsObject(Long userId) {
        JsonNode integrations = getIntegrations(userId);
        if (integrations instanceof ObjectNode objectNode) {
            return objectNode.deepCopy();
        }
        return objectMapper.createObjectNode();
    }

    private ArrayNode schedulesArray(ObjectNode integrations) {
        if (integrations.path("schedules").isArray()) {
            return (ArrayNode) integrations.get("schedules");
        }
        return objectMapper.createArrayNode();
    }

    private JsonNode saveIntegrations(Long userId, ObjectNode integrations) {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("schedules", integrations.get("schedules"));
        return updateIntegrations(userId, patch);
    }

    private JsonNode findSchedule(JsonNode integrations, String scheduleId) {
        for (JsonNode schedule : integrations.path("schedules")) {
            if (scheduleId.equals(schedule.path("id").asText())) {
                return schedule;
            }
        }
        throw new ResourceNotFoundException("integrations.schedule.error.notFound:" + scheduleId);
    }
}
