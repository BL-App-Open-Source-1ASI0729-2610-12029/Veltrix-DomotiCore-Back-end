package com.domoticore.devicecontrol.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DevicesOverviewRefreshService {

    private static final String OVERVIEW_COLLECTION = "devices-overview";
    private static final String DETAILS_COLLECTION = "device-details";

    private final UserCollectionAccessService userCollectionAccessService;
    private final ObjectMapper objectMapper;

    public DevicesOverviewRefreshService(
            UserCollectionAccessService userCollectionAccessService,
            ObjectMapper objectMapper) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JsonNode refresh(User user, String segment) {
        List<JsonNode> overviews = userCollectionAccessService.list(user, segment, OVERVIEW_COLLECTION);
        if (overviews.isEmpty()) {
            return objectMapper.createObjectNode().put("refreshed", false);
        }

        JsonNode overview = overviews.get(0);
        String overviewId = overview.path("id").asText("1");
        Map<String, JsonNode> detailsById = indexDetails(user, segment);

        ObjectNode refreshed = overview.deepCopy();
        ArrayNode rooms = refreshed.path("rooms").isArray()
                ? (ArrayNode) refreshed.get("rooms")
                : objectMapper.createArrayNode();

        int activeCount = 0;
        int totalCount = 0;

        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            JsonNode room = rooms.get(roomIndex);
            if (!room.path("devices").isArray()) {
                continue;
            }
            ArrayNode devices = (ArrayNode) room.get("devices");
            for (int deviceIndex = 0; deviceIndex < devices.size(); deviceIndex++) {
                ObjectNode device = devices.get(deviceIndex).deepCopy();
                String deviceId = device.path("id").asText();
                JsonNode detail = detailsById.get(deviceId);
                if (detail != null) {
                    device.put("active", detail.path("active").asBoolean(detail.path("powerOn").asBoolean(false)));
                    device.put("connection", detail.path("connection").asText(
                            detail.path("status").asText(device.path("connection").asText("online"))));
                    device.put("powerUsageW", detail.path("powerUsageW").asDouble(
                            detail.path("powerLoadKw").asDouble(0) * 1000));
                    device.put("lastSeenAt", Instant.now().toString());
                }
                devices.set(deviceIndex, device);
                totalCount++;
                if (device.path("active").asBoolean(false)) {
                    activeCount++;
                }
            }
        }

        refreshed.set("rooms", rooms);
        refreshed.put("activeDeviceCount", activeCount);
        refreshed.put("totalDevices", totalCount);
        refreshed.put("lastRefreshedAt", Instant.now().toString());

        JsonNode saved = userCollectionAccessService.patch(user, segment, OVERVIEW_COLLECTION, overviewId, refreshed);
        ObjectNode response = objectMapper.createObjectNode();
        response.put("refreshed", true);
        response.put("refreshedAt", Instant.now().toString());
        response.put("totalDevices", totalCount);
        response.put("activeDevices", activeCount);
        response.set("overview", saved);
        return response;
    }

    private Map<String, JsonNode> indexDetails(User user, String segment) {
        Map<String, JsonNode> indexed = new HashMap<>();
        for (JsonNode detail : userCollectionAccessService.list(user, segment, DETAILS_COLLECTION)) {
            indexed.put(detail.path("id").asText(), detail);
        }
        return indexed;
    }
}
