package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-details")
@Tag(name = "Device Detail Commands")
public class DeviceDetailCommandsController {

    private static final String COLLECTION = "device-details";

    private final JsonResourceService jsonResourceService;
    private final ObjectMapper objectMapper;

    public DeviceDetailCommandsController(JsonResourceService jsonResourceService, ObjectMapper objectMapper) {
        this.jsonResourceService = jsonResourceService;
        this.objectMapper = objectMapper;
    }

    @PatchMapping("/{id}/temperature")
    @Operation(summary = "Update device target/current temperature")
    public JsonNode patchTemperature(@PathVariable String id, @RequestBody JsonNode body) {
        ObjectNode patch = objectMapper.createObjectNode();
        if (body.has("targetTempC")) {
            patch.set("targetTempC", body.get("targetTempC"));
        }
        if (body.has("currentTempC")) {
            patch.set("currentTempC", body.get("currentTempC"));
        }
        return jsonResourceService.patch(COLLECTION, id, patch);
    }

    @PatchMapping("/{id}/operation-mode")
    @Operation(summary = "Update device operation mode and eco mode")
    public JsonNode patchOperationMode(@PathVariable String id, @RequestBody JsonNode body) {
        ObjectNode patch = objectMapper.createObjectNode();
        if (body.has("operationMode")) {
            patch.set("operationMode", body.get("operationMode"));
        }
        if (body.has("ecoMode")) {
            patch.set("ecoMode", body.get("ecoMode"));
        }
        if (body.has("fanSpeed")) {
            patch.set("fanSpeed", body.get("fanSpeed"));
        }
        if (body.has("swing")) {
            patch.set("swing", body.get("swing"));
        }
        return jsonResourceService.patch(COLLECTION, id, patch);
    }

    @PatchMapping("/{id}/timer")
    @Operation(summary = "Update device scheduled timer")
    public JsonNode patchTimer(@PathVariable String id, @RequestBody JsonNode body) {
        ObjectNode patch = objectMapper.createObjectNode();
        if (body.has("scheduledTimer")) {
            patch.set("scheduledTimer", body.get("scheduledTimer"));
        }
        return jsonResourceService.patch(COLLECTION, id, patch);
    }
}
