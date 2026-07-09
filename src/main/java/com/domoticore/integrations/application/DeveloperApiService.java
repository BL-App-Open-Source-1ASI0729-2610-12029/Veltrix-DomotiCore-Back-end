package com.domoticore.integrations.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.infrastructure.security.UserDataScopeResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeveloperApiService {

    private static final String DEVICE_DETAILS_COLLECTION = "device-details";

    private final UserCollectionAccessService userCollectionAccessService;
    private final UserDataScopeResolver userDataScopeResolver;
    private final ObjectMapper objectMapper;

    public DeveloperApiService(
            UserCollectionAccessService userCollectionAccessService,
            UserDataScopeResolver userDataScopeResolver,
            ObjectMapper objectMapper) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.userDataScopeResolver = userDataScopeResolver;
        this.objectMapper = objectMapper;
    }

    public JsonNode validateToken(User user) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("valid", true);
        response.put("accountEmail", user.getEmail());
        response.put("segment", resolveSegment(user));
        return response;
    }

    public ArrayNode listDeviceStatuses(User user) {
        String segment = resolveSegment(user);
        List<JsonNode> devices = userCollectionAccessService.list(user, segment, DEVICE_DETAILS_COLLECTION);
        ArrayNode result = objectMapper.createArrayNode();
        for (JsonNode device : devices) {
            result.add(toDeviceStatus(device));
        }
        return result;
    }

    private ObjectNode toDeviceStatus(JsonNode device) {
        ObjectNode status = objectMapper.createObjectNode();
        status.put("id", device.path("id").asText());
        status.put("name", device.path("name").asText(device.path("deviceName").asText("Unknown device")));
        status.put("type", device.path("type").asText(device.path("category").asText("generic")));
        status.put("active", device.path("active").asBoolean(device.path("powerOn").asBoolean(false)));
        status.put("connection", device.path("connection").asText(
                device.path("status").asText(device.path("online").asBoolean(false) ? "online" : "offline")));
        if (device.hasNonNull("batteryPercent")) {
            status.put("batteryPercent", device.get("batteryPercent").asInt());
        }
        if (device.hasNonNull("powerUsageW")) {
            status.put("powerUsageW", device.get("powerUsageW").asDouble());
        }
        if (device.hasNonNull("lastUpdatedAt")) {
            status.put("lastUpdatedAt", device.get("lastUpdatedAt").asText());
        }
        return status;
    }

    private String resolveSegment(User user) {
        return userDataScopeResolver.resolveSegment(user, null);
    }
}
