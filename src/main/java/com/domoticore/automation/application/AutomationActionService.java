package com.domoticore.automation.application;

import com.domoticore.devicecontrol.application.DeviceBulkControlService;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.domain.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class AutomationActionService {

    private static final String SCENES = "automation-active-scenes";
    private static final String SHUTDOWN_PROTOCOL = "automation-shutdown-protocol";
    private static final String DEVICE_DETAILS = "device-details";
    private static final String RULES = "automation-rules";
    private static final String SMART_SUGGESTION = "automation-smart-suggestion";

    private static final Set<String> SHUTDOWN_SCENES = Set.of("away-mode", "night-mode", "closing-time", "movie-night");
    private static final Set<String> START_SCENES = Set.of("morning-routine");

    private final UserCollectionAccessService userCollectionAccessService;
    private final DeviceBulkControlService deviceBulkControlService;
    private final ObjectMapper objectMapper;

    public AutomationActionService(
            UserCollectionAccessService userCollectionAccessService,
            DeviceBulkControlService deviceBulkControlService,
            ObjectMapper objectMapper) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.deviceBulkControlService = deviceBulkControlService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JsonNode activateEcoMode(User user, String segment) {
        List<JsonNode> devices = userCollectionAccessService.list(user, segment, DEVICE_DETAILS);
        ArrayNode affected = objectMapper.createArrayNode();
        ArrayNode skipped = objectMapper.createArrayNode();

        for (JsonNode device : devices) {
            String deviceId = device.path("id").asText();
            boolean priority = device.path("priority").asBoolean(device.path("isPriority").asBoolean(false));
            double powerLoad = device.path("powerLoadKw").asDouble(device.path("powerUsageW").asDouble(0) / 1000.0);
            boolean highConsumption = powerLoad >= 0.8;

            if (priority) {
                ObjectNode patch = objectMapper.createObjectNode();
                patch.put("ecoMode", true);
                userCollectionAccessService.patch(user, segment, DEVICE_DETAILS, deviceId, patch);
                skipped.add(deviceId);
                continue;
            }

            if (!highConsumption || !device.path("active").asBoolean(device.path("powerOn").asBoolean(false))) {
                skipped.add(deviceId);
                continue;
            }

            ObjectNode patch = objectMapper.createObjectNode();
            patch.put("active", false);
            patch.put("powerOn", false);
            patch.put("ecoMode", true);
            patch.put("powerLoadKw", 0);
            userCollectionAccessService.patch(user, segment, DEVICE_DETAILS, deviceId, patch);
            affected.add(deviceId);
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("ecoModeActive", true);
        response.put("messageKey", affected.isEmpty()
                ? "automation.ecoMode.noActions"
                : "automation.ecoMode.activated");
        response.set("devicesTurnedOff", affected);
        response.set("devicesSkipped", skipped);
        response.put("executedAt", Instant.now().toString());
        return response;
    }

    @Transactional
    public JsonNode executeScene(User user, String segment, String sceneId) {
        JsonNode scene = resolveScene(user, segment, sceneId);
        String resolvedId = scene.path("id").asText(sceneId);

        if (!"closing-time".equals(resolvedId)) {
            ObjectNode scenePatch = objectMapper.createObjectNode();
            scenePatch.put("active", true);
            userCollectionAccessService.patch(user, segment, SCENES, resolvedId, scenePatch);
        }

        JsonNode bulkResult;
        if (START_SCENES.contains(resolvedId)) {
            bulkResult = deviceBulkControlService.bulkToggle(user, segment, "on", true);
        } else if (SHUTDOWN_SCENES.contains(resolvedId)) {
            boolean includePriority = !"closing-time".equals(resolvedId);
            bulkResult = deviceBulkControlService.bulkToggle(user, segment, "off", includePriority);
        } else {
            bulkResult = objectMapper.createObjectNode().put("action", "none");
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("sceneId", resolvedId);
        response.put("sceneName", scene.path("name").asText());
        response.put("executed", true);
        response.put("executedAt", Instant.now().toString());
        response.set("scene", scene);
        response.set("deviceActions", bulkResult);
        return response;
    }

    private JsonNode resolveScene(User user, String segment, String sceneId) {
        if ("closing-time".equals(sceneId)) {
            return userCollectionAccessService.getSingleton(user, segment, SHUTDOWN_PROTOCOL, "closing-time");
        }

        try {
            return userCollectionAccessService.getById(user, segment, SCENES, sceneId);
        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException("automation.scene.error.notFound:" + sceneId);
        }
    }

    @Transactional
    public JsonNode createRule(User user, String segment, JsonNode body) {
        String id = body.path("id").asText("rule-" + System.currentTimeMillis());
        ObjectNode rule = objectMapper.createObjectNode();
        rule.put("id", id);
        rule.put("name", body.path("name").asText("Custom Rule"));
        rule.put("description", body.path("description").asText("Custom facility automation scenario."));
        rule.put("icon", body.path("icon").asText("auto_awesome"));
        rule.put("active", body.path("active").asBoolean(true));
        rule.put("group", body.path("group").asText("Custom Group"));
        rule.put("status", body.path("status").asText("ACTIVE"));

        ObjectNode timeline = objectMapper.createObjectNode();
        JsonNode incomingTimeline = body.path("timeline");
        timeline.put("startHour", incomingTimeline.path("startHour").asInt(8));
        timeline.put("endHour", incomingTimeline.path("endHour").asInt(18));
        timeline.put("label", incomingTimeline.path("label").asText(body.path("name").asText("Custom Rule")));
        timeline.put("color", incomingTimeline.path("color").asText("#4263eb"));
        rule.set("timeline", timeline);

        return userCollectionAccessService.create(user, segment, RULES, rule);
    }

    @Transactional
    public JsonNode toggleShutdownStep(User user, String segment, String stepId) {
        JsonNode protocol = userCollectionAccessService.getSingleton(user, segment, SHUTDOWN_PROTOCOL, "closing-time");
        ArrayNode steps = protocol.path("steps").isArray()
                ? (ArrayNode) protocol.get("steps")
                : objectMapper.createArrayNode();

        ArrayNode updatedSteps = objectMapper.createArrayNode();
        boolean found = false;
        for (JsonNode step : steps) {
            ObjectNode copy = step.deepCopy();
            if (stepId.equals(step.path("id").asText())) {
                copy.put("disabled", !step.path("disabled").asBoolean(false));
                found = true;
            }
            updatedSteps.add(copy);
        }

        if (!found) {
            throw new ResourceNotFoundException("automation.shutdown.error.stepNotFound:" + stepId);
        }

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("steps", updatedSteps);
        return userCollectionAccessService.patch(user, segment, SHUTDOWN_PROTOCOL, "closing-time", patch);
    }

    @Transactional
    public JsonNode dismissSmartSuggestion(User user, String segment) {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("visible", false);
        return userCollectionAccessService.patch(user, segment, SMART_SUGGESTION, "default", patch);
    }
}
