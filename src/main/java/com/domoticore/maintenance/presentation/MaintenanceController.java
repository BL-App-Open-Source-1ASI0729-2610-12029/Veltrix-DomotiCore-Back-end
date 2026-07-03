package com.domoticore.maintenance.presentation;

import com.domoticore.maintenance.application.MaintenanceService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.config.openapi.ApiPostCreateResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maintenance-records")
@Tag(name = "Maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final CurrentUserProvider currentUserProvider;

    public MaintenanceController(MaintenanceService maintenanceService, CurrentUserProvider currentUserProvider) {
        this.maintenanceService = maintenanceService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "List maintenance records for the authenticated user")
    public ArrayNode listRecords() {
        return maintenanceService.listRecords(currentUserProvider.requireUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiPostCreateResponses
    @Operation(summary = "Register a completed maintenance visit")
    public JsonNode registerRecord(@RequestBody JsonNode body) {
        currentUserProvider.requirePermission(com.domoticore.shared.security.PlatformPermission.MAINTENANCE_REGISTER);
        return maintenanceService.registerRecord(currentUserProvider.requireUserId(), body);
    }
}
