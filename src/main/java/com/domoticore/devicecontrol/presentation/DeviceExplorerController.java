package com.domoticore.devicecontrol.presentation;

import com.domoticore.devicecontrol.application.DeviceExplorerService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-explorer")
@Tag(name = "Device Explorer")
public class DeviceExplorerController {

    private final DeviceExplorerService deviceExplorerService;
    private final CurrentUserProvider currentUserProvider;

    public DeviceExplorerController(
            DeviceExplorerService deviceExplorerService,
            CurrentUserProvider currentUserProvider) {
        this.deviceExplorerService = deviceExplorerService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get facility device explorer snapshot")
    public JsonNode getDeviceExplorer() {
        var user = currentUserProvider.requireUser();
        return deviceExplorerService.getDeviceExplorer(user, currentUserProvider.requireSegment());
    }
}
