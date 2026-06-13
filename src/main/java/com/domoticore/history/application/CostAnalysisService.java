package com.domoticore.history.application;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class CostAnalysisService {

    private static final String COLLECTION = "cost-analysis";
    private static final String DEFAULT_ID = "default";

    private final JsonResourceService jsonResourceService;

    public CostAnalysisService(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    public JsonNode getCostAnalysis() {
        return stripInternalId(jsonResourceService.findSingleton(COLLECTION, DEFAULT_ID));
    }

    private JsonNode stripInternalId(JsonNode payload) {
        if (payload instanceof ObjectNode objectNode) {
            ObjectNode copy = objectNode.deepCopy();
            copy.remove("id");
            return copy;
        }
        return payload;
    }
}
