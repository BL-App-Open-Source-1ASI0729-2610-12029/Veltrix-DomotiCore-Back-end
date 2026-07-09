package com.domoticore.automation.presentation;

import com.domoticore.automation.application.ZoneConfigurationService;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedPatchResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/zone-configuration")
@Tag(name = "Zone Configuration")
public class ZoneConfigurationController {

    private final ZoneConfigurationService zoneConfigurationService;
    private final CurrentUserProvider currentUserProvider;

    public ZoneConfigurationController(
            ZoneConfigurationService zoneConfigurationService,
            CurrentUserProvider currentUserProvider) {
        this.zoneConfigurationService = zoneConfigurationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get SME zone configuration snapshot")
    public JsonNode getZoneConfiguration() {
        return zoneConfigurationService.getConfiguration(currentUserProvider.requireUserId());
    }

    @PatchMapping
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Update zone configuration (budgets, schedules, monitoring)")
    public JsonNode patchZoneConfiguration(@RequestBody JsonNode body) {
        return zoneConfigurationService.updateConfiguration(currentUserProvider.requireUserId(), body);
    }
}
