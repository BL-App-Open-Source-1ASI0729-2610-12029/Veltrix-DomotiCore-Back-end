package com.domoticore.history.application;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class EnergyIntelligenceService {

    private static final String COLLECTION = "energy-intelligence";
    private static final Set<String> ALLOWED_PERIODS = Set.of("day", "week", "month");
    private static final String DEFAULT_PERIOD = "week";

    private final JsonResourceService jsonResourceService;
    private final ObjectMapper objectMapper;

    public EnergyIntelligenceService(JsonResourceService jsonResourceService, ObjectMapper objectMapper) {
        this.jsonResourceService = jsonResourceService;
        this.objectMapper = objectMapper;
    }

    public JsonNode getEnergyIntelligence(String period) {
        String resolvedPeriod = period == null || period.isBlank() ? DEFAULT_PERIOD : period.trim();
        if (!ALLOWED_PERIODS.contains(resolvedPeriod)) {
            throw new IllegalArgumentException("Invalid period: " + period);
        }
        JsonNode snapshot = jsonResourceService.findById(COLLECTION, resolvedPeriod);
        return enrichSnapshot(snapshot, resolvedPeriod);
    }

    private JsonNode enrichSnapshot(JsonNode snapshot, String period) {
        if (!(snapshot instanceof ObjectNode objectNode)) {
            return snapshot;
        }

        ObjectNode enriched = objectNode.deepCopy();
        if (!enriched.has("savingsSuggestions") || !enriched.get("savingsSuggestions").isArray()) {
            enriched.set("savingsSuggestions", defaultSavingsSuggestions(period));
        }
        if (!enriched.has("anomalies") || !enriched.get("anomalies").isArray()) {
            enriched.set("anomalies", defaultAnomalies());
        }
        return enriched;
    }

    private ArrayNode defaultSavingsSuggestions(String period) {
        double factor = switch (period) {
            case "day" -> 0.14;
            case "month" -> 4.3;
            default -> 1.0;
        };

        ArrayNode suggestions = objectMapper.createArrayNode();
        suggestions.add(suggestion("save-thermostat", "Lower thermostat by 2°C",
                "Shift HVAC setpoint during peak hours to reduce HVAC load.", round(4.2 * factor)));
        suggestions.add(suggestion("save-standby", "Disable standby devices overnight",
                "Entertainment and office peripherals still draw power when idle.", round(1.8 * factor)));
        suggestions.add(suggestion("save-lighting", "Dim non-priority lighting 30%",
                "Apply scene-based dimming in low-traffic areas after 10 PM.", round(0.9 * factor)));
        return suggestions;
    }

    private ObjectNode suggestion(String id, String title, String description, double savingKwh) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("title", title);
        node.put("description", description);
        node.put("estimatedSavingKwh", savingKwh);
        return node;
    }

    private ArrayNode defaultAnomalies() {
        ArrayNode anomalies = objectMapper.createArrayNode();

        ObjectNode hvac = objectMapper.createObjectNode();
        hvac.put("id", "anom-hvac");
        hvac.put("deviceName", "HVAC System");
        hvac.put("severity", "high");
        hvac.put("message", "Consumption 38% above the 7-day average between 2–4 PM.");
        hvac.put("detectedAt", Instant.now().minusSeconds(3600).toString());
        anomalies.add(hvac);

        ObjectNode fridge = objectMapper.createObjectNode();
        fridge.put("id", "anom-fridge");
        fridge.put("deviceName", "Refrigerator");
        fridge.put("severity", "medium");
        fridge.put("message", "Compressor cycles are unusually frequent overnight.");
        fridge.put("detectedAt", Instant.now().minusSeconds(7200).toString());
        anomalies.add(fridge);

        return anomalies;
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
