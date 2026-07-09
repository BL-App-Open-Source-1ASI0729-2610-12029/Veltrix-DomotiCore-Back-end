package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.infrastructure.config.openapi.ApiPatchMutationResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
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

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/device-details/{id}")
@Tag(name = "Device Detail Operations")
public class DeviceDetailOperationsController {

    private static final String COLLECTION = "device-details";

    private final UserCollectionAccessService userCollectionAccessService;
    private final CurrentUserProvider currentUserProvider;
    private final ObjectMapper objectMapper;

    public DeviceDetailOperationsController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider,
            ObjectMapper objectMapper) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.currentUserProvider = currentUserProvider;
        this.objectMapper = objectMapper;
    }

    @PatchMapping("/temperature")
    @ApiPatchMutationResponses
    @Operation(summary = "Adjust target temperature for a climate device")
    public JsonNode patchTemperature(@PathVariable String id, @RequestBody ObjectNode body) {
        var user = currentUserProvider.requireUser();
        String segment = currentUserProvider.requireSegment();
        JsonNode current = userCollectionAccessService.getById(user, segment, COLLECTION, id);
        double target = body.path("targetTempC").asDouble(current.path("targetTempC").asDouble(21));
        double clamped = Math.min(30, Math.max(16, target));

        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("targetTempC", clamped);
        if (current.has("currentTempC")) {
            double currentTemp = current.path("currentTempC").asDouble();
            double direction = clamped > currentTemp ? 0.3 : -0.3;
            patch.put("currentTempC", Math.round((currentTemp + direction) * 10) / 10.0);
        }
        patch.put("lastStateAt", Instant.now().toString());
        patch.put("lastStateLabel", "Temperature updated");
        return userCollectionAccessService.patch(user, segment, COLLECTION, id, patch);
    }

    @PatchMapping("/operation-mode")
    @ApiPatchMutationResponses
    @Operation(summary = "Set operation mode for a climate device")
    public JsonNode patchOperationMode(@PathVariable String id, @RequestBody ObjectNode body) {
        var user = currentUserProvider.requireUser();
        String segment = currentUserProvider.requireSegment();
        String mode = body.path("operationMode").asText("cool");
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("operationMode", mode);
        patch.put("lastStateAt", Instant.now().toString());
        patch.put("lastStateLabel", "Mode: " + mode);
        return userCollectionAccessService.patch(user, segment, COLLECTION, id, patch);
    }

    @PatchMapping("/timer")
    @ApiPatchMutationResponses
    @Operation(summary = "Set or clear scheduled timer")
    public JsonNode patchTimer(@PathVariable String id, @RequestBody ObjectNode body) {
        var user = currentUserProvider.requireUser();
        String segment = currentUserProvider.requireSegment();
        ObjectNode patch = objectMapper.createObjectNode();
        if (body.has("scheduledTimer") && !body.get("scheduledTimer").isNull()) {
            patch.put("scheduledTimer", body.path("scheduledTimer").asText());
            patch.put("lastStateLabel", "Timer scheduled");
        } else {
            patch.putNull("scheduledTimer");
            patch.put("lastStateLabel", "Timer cleared");
        }
        patch.put("lastStateAt", Instant.now().toString());
        return userCollectionAccessService.patch(user, segment, COLLECTION, id, patch);
    }

    @PatchMapping("/rename")
    @ApiPatchMutationResponses
    @Operation(summary = "Rename a device and propagate last-state metadata")
    public JsonNode renameDevice(@PathVariable String id, @RequestBody ObjectNode body) {
        var user = currentUserProvider.requireUser();
        String segment = currentUserProvider.requireSegment();
        String name = body.path("name").asText("").trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("device.error.nameRequired");
        }
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("name", name);
        patch.put("lastStateAt", Instant.now().toString());
        patch.put("lastStateLabel", "Renamed");
        return userCollectionAccessService.patch(user, segment, COLLECTION, id, patch);
    }
}
