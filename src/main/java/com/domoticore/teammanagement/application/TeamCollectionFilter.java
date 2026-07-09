package com.domoticore.teammanagement.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public final class TeamCollectionFilter {

    private TeamCollectionFilter() {
    }

    public static List<JsonNode> filterList(String collectionName, List<JsonNode> items, List<String> zones) {
        if (TeamZoneAccess.hasGlobalAccess(zones)) {
            return items;
        }
        return items.stream()
                .filter(item -> itemMatchesCollection(collectionName, item, zones))
                .toList();
    }

    public static JsonNode filterNode(String collectionName, JsonNode node, List<String> zones) {
        if (node == null || TeamZoneAccess.hasGlobalAccess(zones)) {
            return node;
        }
        if (!(node instanceof ObjectNode objectNode)) {
            return node;
        }

        return switch (collectionName) {
            case "devices-overview" -> filterDevicesOverview(objectNode, zones);
            case "business-devices-overview" -> filterBusinessOverview(objectNode, zones);
            default -> node;
        };
    }

    private static boolean itemMatchesCollection(String collectionName, JsonNode item, List<String> zones) {
        return switch (collectionName) {
            case "device-details" -> TeamZoneAccess.canAccessZone(zones, item.path("roomId").asText(""));
            default -> true;
        };
    }

    private static ObjectNode filterDevicesOverview(ObjectNode overview, List<String> zones) {
        ObjectNode copy = overview.deepCopy();
        if (!copy.path("rooms").isArray()) {
            return copy;
        }

        ArrayNode rooms = copy.putArray("rooms");
        for (JsonNode roomNode : overview.get("rooms")) {
            String roomId = roomNode.path("id").asText("");
            if (TeamZoneAccess.canAccessZone(zones, roomId)) {
                rooms.add(roomNode);
            }
        }
        copy.put("totalRooms", rooms.size());
        return copy;
    }

    private static ObjectNode filterBusinessOverview(ObjectNode overview, List<String> zones) {
        ObjectNode copy = overview.deepCopy();
        if (!copy.path("zones").isArray()) {
            return copy;
        }

        ArrayNode filteredZones = copy.putArray("zones");
        int activeDevices = 0;
        for (JsonNode zoneNode : overview.get("zones")) {
            String zoneId = zoneNode.path("id").asText("");
            if (TeamZoneAccess.canAccessZone(zones, zoneId)) {
                filteredZones.add(zoneNode);
                activeDevices += countActiveDevices(zoneNode);
            }
        }
        copy.put("zoneCount", filteredZones.size());
        copy.put("activeDeviceCount", activeDevices);
        return copy;
    }

    private static int countActiveDevices(JsonNode zoneNode) {
        int count = 0;
        if (zoneNode.path("cards").isArray()) {
            for (JsonNode card : zoneNode.get("cards")) {
                if (card.path("active").asBoolean(false)) {
                    count++;
                }
            }
        }
        if (zoneNode.path("tableRows").isArray()) {
            for (JsonNode row : zoneNode.get("tableRows")) {
                if (row.path("active").asBoolean(false)) {
                    count++;
                }
            }
        }
        if (zoneNode.path("lightingGroup").path("active").asBoolean(false)) {
            count += zoneNode.path("lightingGroup").path("activeUnits").asInt(0);
        }
        return count;
    }
}
