package com.domoticore.dashboard.presentation;

import com.domoticore.dashboard.application.DashboardService;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserProvider currentUserProvider;

    public DashboardController(DashboardService dashboardService, CurrentUserProvider currentUserProvider) {
        this.dashboardService = dashboardService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get dashboard overview with device totals, alerts and energy data")
    public JsonNode getDashboard() {
        var user = currentUserProvider.requireUser();
        return dashboardService.getDashboard(user, currentUserProvider.requireSegment());
    }
}
