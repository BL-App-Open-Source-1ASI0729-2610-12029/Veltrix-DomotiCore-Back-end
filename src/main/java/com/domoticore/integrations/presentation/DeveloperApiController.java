package com.domoticore.integrations.presentation;

import com.domoticore.integrations.application.DeveloperApiKeyService;
import com.domoticore.integrations.application.DeveloperApiService;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/developer")
@Tag(name = "Developer API")
public class DeveloperApiController {

    private final DeveloperApiService developerApiService;
    private final CurrentUserProvider currentUserProvider;

    public DeveloperApiController(
            DeveloperApiService developerApiService,
            CurrentUserProvider currentUserProvider) {
        this.developerApiService = developerApiService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/auth/validate")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Validate developer API access token")
    public JsonNode validateToken() {
        return developerApiService.validateToken(currentUserProvider.requireUser());
    }

    @GetMapping("/devices")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "List device statuses for external integrations")
    public ArrayNode listDevices() {
        return developerApiService.listDeviceStatuses(currentUserProvider.requireUser());
    }

    @GetMapping("/devices/{id}")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get device status by id for external integrations")
    public JsonNode getDevice(@PathVariable String id) {
        return developerApiService.getDeviceStatus(currentUserProvider.requireUser(), id);
    }
}
