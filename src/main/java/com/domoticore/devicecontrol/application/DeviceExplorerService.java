package com.domoticore.devicecontrol.application;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class DeviceExplorerService {

    private static final String COLLECTION = "device-explorer";
    private static final String DEFAULT_TEMPLATE = "default";

    private final JsonResourceService jsonResourceService;

    public DeviceExplorerService(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    public JsonNode getDeviceExplorer() {
        return jsonResourceService.findById(COLLECTION, DEFAULT_TEMPLATE);
    }
}
