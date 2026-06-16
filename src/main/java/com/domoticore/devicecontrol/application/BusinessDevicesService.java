package com.domoticore.devicecontrol.application;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class BusinessDevicesService {

    private static final String COLLECTION = "business-devices-overview";
    private static final String DEFAULT_TEMPLATE = "default";

    private final JsonResourceService jsonResourceService;

    public BusinessDevicesService(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    public JsonNode getOverview() {
        return jsonResourceService.findById(COLLECTION, DEFAULT_TEMPLATE);
    }
}
