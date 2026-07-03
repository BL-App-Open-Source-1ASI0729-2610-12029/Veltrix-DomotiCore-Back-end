package com.domoticore.history.presentation;

import com.domoticore.history.application.EnergyIntelligenceService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.security.CurrentUserProvider;
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
    private final CurrentUserProvider currentUserProvider;

    public EnergyIntelligenceController(
            EnergyIntelligenceService energyIntelligenceService,
            CurrentUserProvider currentUserProvider) {
        this.energyIntelligenceService = energyIntelligenceService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get home energy intelligence snapshot for a period")
    public JsonNode getEnergyIntelligence(
            @RequestParam(defaultValue = "week") String period) {
        var user = currentUserProvider.requireUser();
        return energyIntelligenceService.getEnergyIntelligence(user, currentUserProvider.requireSegment(), period);
    }
}
