package com.domoticore.integrations.presentation;

import com.domoticore.integrations.application.IntegrationsService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.config.openapi.ApiAuthenticatedPatchResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.shared.security.PlatformPermission;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
@Tag(name = "Smart Integrations")
public class IntegrationsController {

    private final IntegrationsService integrationsService;
    private final CurrentUserProvider currentUserProvider;

    public IntegrationsController(
            IntegrationsService integrationsService,
            CurrentUserProvider currentUserProvider) {
        this.integrationsService = integrationsService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get smart integrations, schedules and compatibility catalog")
    public JsonNode getIntegrations() {
        return integrationsService.getIntegrations(currentUserProvider.requireUserId());
    }

    @PatchMapping
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Update smart integrations configuration")
    public JsonNode patchIntegrations(@RequestBody JsonNode body) {
        currentUserProvider.requirePermission(PlatformPermission.INTEGRATIONS_MANAGE);
        return integrationsService.updateIntegrations(currentUserProvider.requireUserId(), body);
    }

    @PostMapping("/compatibility-check")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Check whether a device model or type is compatible with DomotiCore")
    public JsonNode checkCompatibility(@RequestBody ObjectNode body) {
        String modelOrType = body.path("modelOrType").asText(body.path("model").asText(null));
        return integrationsService.checkCompatibility(currentUserProvider.requireUserId(), modelOrType);
    }
}
