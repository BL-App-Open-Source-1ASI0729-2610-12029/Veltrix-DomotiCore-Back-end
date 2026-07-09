package com.domoticore.automation.presentation;

import com.domoticore.automation.application.AutomationHomePreferencesService;
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
@RequestMapping("/api/v1/automation/home-preferences")
@Tag(name = "Automation Home Preferences")
public class AutomationHomePreferencesController {

    private final AutomationHomePreferencesService preferencesService;
    private final CurrentUserProvider currentUserProvider;

    public AutomationHomePreferencesController(
            AutomationHomePreferencesService preferencesService,
            CurrentUserProvider currentUserProvider) {
        this.preferencesService = preferencesService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get smart-home automation preferences (inactivity auto-off, auto optimization)")
    public JsonNode getPreferences() {
        return preferencesService.getPreferences(currentUserProvider.requireUserId());
    }

    @PatchMapping
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Update smart-home automation preferences")
    public JsonNode patchPreferences(@RequestBody JsonNode body) {
        return preferencesService.updatePreferences(currentUserProvider.requireUserId(), body);
    }
}
