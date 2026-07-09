package com.domoticore.devicecontrol.presentation;

import com.domoticore.devicecontrol.application.DeviceBulkControlService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-details")
@Tag(name = "Device Bulk Control")
public class DeviceBulkControlController {

    private final DeviceBulkControlService deviceBulkControlService;
    private final CurrentUserProvider currentUserProvider;

    public DeviceBulkControlController(
            DeviceBulkControlService deviceBulkControlService,
            CurrentUserProvider currentUserProvider) {
        this.deviceBulkControlService = deviceBulkControlService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/bulk-toggle")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Turn all devices on or off in a single operation")
    public JsonNode bulkToggle(@RequestBody ObjectNode body) {
        var user = currentUserProvider.requireUser();
        String action = body.path("action").asText("off");
        boolean includePriority = body.path("includePriority").asBoolean(true);
        return deviceBulkControlService.bulkToggle(user, currentUserProvider.requireSegment(), action, includePriority);
    }
}
