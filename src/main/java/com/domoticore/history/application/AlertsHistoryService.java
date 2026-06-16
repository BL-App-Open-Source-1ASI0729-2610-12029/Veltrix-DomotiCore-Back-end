package com.domoticore.history.application;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class AlertsHistoryService {

    private static final String COLLECTION = "alerts-history";
    private static final String DEFAULT_TEMPLATE = "default";

    private final JsonResourceService jsonResourceService;

    public AlertsHistoryService(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    public JsonNode getAlertsHistory() {
        return jsonResourceService.findById(COLLECTION, DEFAULT_TEMPLATE);
    }
}
