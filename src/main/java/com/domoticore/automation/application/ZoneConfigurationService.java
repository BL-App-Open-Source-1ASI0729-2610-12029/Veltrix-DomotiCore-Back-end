package com.domoticore.automation.application;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZoneConfigurationService {

    private static final String COLLECTION = "zone-configuration";
    private static final String DEFAULT_ID = "default";

    private final JsonResourceService jsonResourceService;

    public ZoneConfigurationService(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    public JsonNode getConfiguration() {
        return stripInternalId(jsonResourceService.findSingleton(COLLECTION, DEFAULT_ID));
    }

    @Transactional
    public JsonNode updateConfiguration(JsonNode patch) {
        return stripInternalId(jsonResourceService.patch(COLLECTION, DEFAULT_ID, patch));
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
