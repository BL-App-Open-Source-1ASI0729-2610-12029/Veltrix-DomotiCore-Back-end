package com.domoticore.history.presentation;

import com.domoticore.history.application.EnergyIntelligenceService;
import com.domoticore.shared.config.openapi.ApiGetByIdResponses;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/energy-intelligence")
@Tag(name = "Energy Intelligence")
public class EnergyIntelligenceController {

    private final EnergyIntelligenceService energyIntelligenceService;

    public EnergyIntelligenceController(EnergyIntelligenceService energyIntelligenceService) {
        this.energyIntelligenceService = energyIntelligenceService;
    }

    @GetMapping
    @ApiGetByIdResponses
    @Operation(summary = "Get home energy intelligence snapshot for a period")
    public JsonNode getEnergyIntelligence(
            @RequestParam(defaultValue = "week") String period) {
        return energyIntelligenceService.getEnergyIntelligence(period);
    }
}
