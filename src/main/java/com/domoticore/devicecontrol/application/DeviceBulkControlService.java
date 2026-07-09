package com.domoticore.devicecontrol.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeviceBulkControlService {

    private static final String COLLECTION = "device-details";

    private final UserCollectionAccessService userCollectionAccessService;
    private final ObjectMapper objectMapper;

    public DeviceBulkControlService(
            UserCollectionAccessService userCollectionAccessService,
            ObjectMapper objectMapper) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JsonNode bulkToggle(User user, String segment, String action, boolean includePriority) {
        boolean targetActive = "on".equalsIgnoreCase(action);
        List<JsonNode> devices = userCollectionAccessService.list(user, segment, COLLECTION);

        ArrayNode succeeded = objectMapper.createArrayNode();
        ArrayNode failed = objectMapper.createArrayNode();

        for (JsonNode device : devices) {
            String deviceId = device.path("id").asText();
            if (!includePriority && !targetActive && device.path("priority").asBoolean(device.path("isPriority").asBoolean(false))) {
                continue;
            }

            ObjectNode patch = objectMapper.createObjectNode();
            patch.put("active", targetActive);
            patch.put("powerOn", targetActive);
            if (device.has("connection")) {
                patch.put("connection", device.path("connection").asText("online"));
            }

            try {
                userCollectionAccessService.patch(user, segment, COLLECTION, deviceId, patch);
                succeeded.add(deviceId);
            } catch (RuntimeException ex) {
                ObjectNode failure = objectMapper.createObjectNode();
                failure.put("id", deviceId);
                failure.put("name", device.path("name").asText(device.path("deviceName").asText(deviceId)));
                failure.put("reason", "DEVICE_CONNECTION_ERROR");
                failed.add(failure);
            }
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("action", targetActive ? "on" : "off");
        response.put("includePriority", includePriority);
        response.set("succeeded", succeeded);
        response.set("failed", failed);
        return response;
    }
}
