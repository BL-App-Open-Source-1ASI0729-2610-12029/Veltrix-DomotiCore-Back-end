package com.domoticore.automation.presentation;

import com.domoticore.automation.application.ZoneConfigurationService;
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

    public ZoneConfigurationController(ZoneConfigurationService zoneConfigurationService) {
        this.zoneConfigurationService = zoneConfigurationService;
    }

    @GetMapping
    @Operation(summary = "Get SME zone configuration snapshot")
    public JsonNode getZoneConfiguration() {
        return zoneConfigurationService.getConfiguration();
    }

    @PatchMapping
    @Operation(summary = "Update zone configuration (budgets, schedules, monitoring)")
    public JsonNode patchZoneConfiguration(@RequestBody JsonNode body) {
        return zoneConfigurationService.updateConfiguration(body);
    }
}
