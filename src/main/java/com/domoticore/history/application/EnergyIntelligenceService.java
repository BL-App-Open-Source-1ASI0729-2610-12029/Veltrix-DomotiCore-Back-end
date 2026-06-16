package com.domoticore.history.application;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class EnergyIntelligenceService {

    private static final String COLLECTION = "energy-intelligence";
    private static final Set<String> ALLOWED_PERIODS = Set.of("day", "week", "month");
    private static final String DEFAULT_PERIOD = "week";

    private final JsonResourceService jsonResourceService;

    public EnergyIntelligenceService(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    public JsonNode getEnergyIntelligence(String period) {
        String resolvedPeriod = period == null || period.isBlank() ? DEFAULT_PERIOD : period.trim();
        if (!ALLOWED_PERIODS.contains(resolvedPeriod)) {
            throw new IllegalArgumentException("Invalid period: " + period);
        }
        return jsonResourceService.findById(COLLECTION, resolvedPeriod);
    }
}
