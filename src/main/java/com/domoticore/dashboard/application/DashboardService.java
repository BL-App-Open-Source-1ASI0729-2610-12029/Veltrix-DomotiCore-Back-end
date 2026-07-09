package com.domoticore.dashboard.application;

import com.domoticore.history.application.EnergyIntelligenceService;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private static final String OVERVIEW_COLLECTION = "devices-overview";
    private static final String NOTIFICATION_COLLECTION = "notification-feed";

    private final UserCollectionAccessService userCollectionAccessService;
    private final EnergyIntelligenceService energyIntelligenceService;
    private final ObjectMapper objectMapper;

    public DashboardService(
            UserCollectionAccessService userCollectionAccessService,
            EnergyIntelligenceService energyIntelligenceService,
            ObjectMapper objectMapper) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.energyIntelligenceService = energyIntelligenceService;
        this.objectMapper = objectMapper;
    }

    public JsonNode getDashboard(User user, String segment) {
        ObjectNode dashboard = objectMapper.createObjectNode();

        JsonNode overview = resolveOverview(user, segment);
        int totalDevices = overview.path("totalDevices").asInt(0);
        double totalConsumptionKwh = overview.path("totalConsumptionKwh").asDouble(0);

        int activeDevices = countActiveDevices(overview);
        int offlineDevices = countOfflineDevices(overview);

        dashboard.put("totalDevices", totalDevices);
        dashboard.put("activeDevices", activeDevices);
        dashboard.put("offlineDevices", offlineDevices);
        dashboard.put("hasDevices", totalDevices > 0);
        dashboard.put("totalConsumptionKwh", totalConsumptionKwh);
        dashboard.put("totalConsumptionLabel", formatConsumption(totalConsumptionKwh));
        dashboard.set("disconnectedDevices", buildDisconnectedDevices(overview));
        dashboard.set("statistics", buildStatistics(totalDevices, activeDevices, totalConsumptionKwh));
        dashboard.set("alerts", buildAlerts(user, segment));
        dashboard.set("devices", buildDevices(overview));
        dashboard.set("energyData", buildEnergyData(user, segment));

        return dashboard;
    }

    private JsonNode resolveOverview(User user, String segment) {
        List<JsonNode> overviews = userCollectionAccessService.list(user, segment, OVERVIEW_COLLECTION);
        if (!overviews.isEmpty()) {
            return overviews.get(0);
        }
        return objectMapper.createObjectNode();
    }

    private int countActiveDevices(JsonNode overview) {
        int count = 0;
        JsonNode rooms = overview.path("rooms");
        if (!rooms.isArray()) {
            return 0;
        }
        for (JsonNode room : rooms) {
            JsonNode devices = room.path("devices");
            if (!devices.isArray()) {
                continue;
            }
            for (JsonNode device : devices) {
                if (device.path("active").asBoolean(false)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countOfflineDevices(JsonNode overview) {
        int count = 0;
        JsonNode rooms = overview.path("rooms");
        if (!rooms.isArray()) {
            return 0;
        }
        for (JsonNode room : rooms) {
            JsonNode devices = room.path("devices");
            if (!devices.isArray()) {
                continue;
            }
            for (JsonNode device : devices) {
                if ("offline".equalsIgnoreCase(device.path("connection").asText())) {
                    count++;
                }
            }
        }
        return count;
    }

    private ArrayNode buildStatistics(int totalDevices, int activeDevices, double totalConsumptionKwh) {
        ArrayNode statistics = objectMapper.createArrayNode();

        ObjectNode power = objectMapper.createObjectNode();
        power.put("titleKey", "dashboard.stats.currentPower.title");
        power.put("value", formatConsumption(totalConsumptionKwh));
        power.put("descriptionKey", "dashboard.stats.currentPower.description");
        power.put("icon", "power");
        statistics.add(power);

        ObjectNode devices = objectMapper.createObjectNode();
        devices.put("titleKey", "dashboard.stats.activeDevices.title");
        devices.put("value", activeDevices + " / " + totalDevices);
        devices.put("descriptionKey", "dashboard.stats.activeDevices.description");
        devices.put("icon", "signal");
        statistics.add(devices);

        ObjectNode savings = objectMapper.createObjectNode();
        savings.put("titleKey", "dashboard.stats.monthlySavings.title");
        savings.put("value", "$" + String.format("%.2f", totalConsumptionKwh * 12.8));
        savings.put("descriptionKey", "dashboard.stats.monthlySavings.description");
        savings.put("icon", "savings");
        statistics.add(savings);

        return statistics;
    }

    private ArrayNode buildDisconnectedDevices(JsonNode overview) {
        ArrayNode disconnected = objectMapper.createArrayNode();
        JsonNode rooms = overview.path("rooms");
        if (!rooms.isArray()) {
            return disconnected;
        }
        for (JsonNode room : rooms) {
            JsonNode roomDevices = room.path("devices");
            if (!roomDevices.isArray()) {
                continue;
            }
            for (JsonNode device : roomDevices) {
                if (!"offline".equalsIgnoreCase(device.path("connection").asText())) {
                    continue;
                }
                ObjectNode entry = objectMapper.createObjectNode();
                entry.put("id", device.path("id").asText());
                entry.put("name", device.path("name").asText());
                entry.put("room", room.path("name").asText());
                entry.put("lastSeenAt", device.path("lastSeenAt").asText(""));
                disconnected.add(entry);
            }
        }
        return disconnected;
    }

    private ArrayNode buildAlerts(User user, String segment) {
        ArrayNode alerts = objectMapper.createArrayNode();
        List<JsonNode> feed = userCollectionAccessService.list(user, segment, NOTIFICATION_COLLECTION);
        int limit = Math.min(feed.size(), 5);
        for (int i = 0; i < limit; i++) {
            JsonNode item = feed.get(i);
            ObjectNode alert = objectMapper.createObjectNode();
            alert.put("typeKey", item.path("titleKey").asText("dashboard.alerts.info.type"));
            alert.put("titleKey", item.path("titleKey").asText());
            alert.put("descriptionKey", item.path("descriptionKey").asText());
            alert.put("timeKey", item.path("timeLabel").asText("dashboard.alerts.info.time"));
            alert.put("danger", "critical".equalsIgnoreCase(item.path("severity").asText()));
            alerts.add(alert);
        }
        return alerts;
    }

    private ArrayNode buildDevices(JsonNode overview) {
        ArrayNode devices = objectMapper.createArrayNode();
        JsonNode rooms = overview.path("rooms");
        if (!rooms.isArray()) {
            return devices;
        }

        for (JsonNode room : rooms) {
            JsonNode roomDevices = room.path("devices");
            if (!roomDevices.isArray()) {
                continue;
            }
            for (JsonNode device : roomDevices) {
                ObjectNode entry = objectMapper.createObjectNode();
                entry.put("id", device.path("id").asText());
                entry.put("name", device.path("name").asText());
                entry.put("active", device.path("active").asBoolean(false));
                entry.put("icon", device.path("icon").asText("devices"));
                entry.put("live", "online".equalsIgnoreCase(device.path("connection").asText()));
                entry.put("usageCategory", inferUsageCategory(device.path("icon").asText()));
                if ("offline".equalsIgnoreCase(device.path("connection").asText())) {
                    entry.put("statusKey", "dashboard.devices.offline");
                } else if (device.path("active").asBoolean(false)) {
                    entry.put("statusKey", "common.on");
                } else {
                    entry.put("statusKey", "common.off");
                }
                devices.add(entry);
            }
        }
        return devices;
    }

    private ObjectNode buildEnergyData(User user, String segment) {
        ObjectNode energyData = objectMapper.createObjectNode();
        energyData.set("24h", mapEnergyRange(energyIntelligenceService.getEnergyIntelligence(user, segment, "day"), "24h"));
        energyData.set("7d", mapEnergyRange(energyIntelligenceService.getEnergyIntelligence(user, segment, "week"), "7d"));
        energyData.set("30d", mapEnergyRange(energyIntelligenceService.getEnergyIntelligence(user, segment, "month"), "30d"));
        return energyData;
    }

    private ObjectNode mapEnergyRange(JsonNode snapshot, String rangeKey) {
        ObjectNode range = objectMapper.createObjectNode();
        range.put("range", rangeKey);
        range.put("titleKey", "dashboard.energy." + rangeKey + ".title");
        range.put("descriptionKey", "dashboard.energy." + rangeKey + ".description");
        range.put("unit", "kW");
        range.put("total", snapshot.path("totalConsumptionKwh").asDouble(0));
        range.put("average", snapshot.path("dailyAverageKwh").asDouble(snapshot.path("totalConsumptionKwh").asDouble(0)));
        range.put("peak", peakFromChart(snapshot.path("chartPoints")));
        range.set("dataPoints", mapChartPoints(snapshot.path("chartPoints")));
        ObjectNode trends = objectMapper.createObjectNode();
        trends.put("comparisonKey", "dashboard.energy." + rangeKey + ".comparison");
        trends.put("insightKey", "dashboard.energy." + rangeKey + ".insight");
        range.set("trends", trends);
        return range;
    }

    private double peakFromChart(JsonNode chartPoints) {
        double peak = 0;
        if (!chartPoints.isArray()) {
            return peak;
        }
        for (JsonNode point : chartPoints) {
            peak = Math.max(peak, point.path("value").asDouble(0));
        }
        return peak;
    }

    private ArrayNode mapChartPoints(JsonNode chartPoints) {
        ArrayNode dataPoints = objectMapper.createArrayNode();
        if (!chartPoints.isArray()) {
            return dataPoints;
        }
        for (JsonNode point : chartPoints) {
            ObjectNode mapped = objectMapper.createObjectNode();
            mapped.put("time", point.path("label").asText());
            double value = point.path("value").asDouble(0);
            mapped.put("value", value);
            mapped.put("status", value >= 1.4 ? "peak" : value <= 0.5 ? "low" : "normal");
            dataPoints.add(mapped);
        }
        return dataPoints;
    }

    private String inferUsageCategory(String icon) {
        String normalized = icon == null ? "" : icon.toLowerCase();
        if (normalized.contains("light")) {
            return "lighting";
        }
        if (normalized.contains("ac") || normalized.contains("thermo")) {
            return "climate";
        }
        if (normalized.contains("cam") || normalized.contains("lock") || normalized.contains("door")) {
            return "security";
        }
        if (normalized.contains("tv")) {
            return "entertainment";
        }
        return "generic";
    }

    private String formatConsumption(double kwh) {
        if (kwh >= 1) {
            return String.format("%.1f kW", kwh);
        }
        return String.format("%.0f W", kwh * 1000);
    }
}
