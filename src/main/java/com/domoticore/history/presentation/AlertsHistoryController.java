package com.domoticore.history.presentation;

import com.domoticore.history.application.AlertsHistoryService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts-history")
@Tag(name = "Alerts History")
public class AlertsHistoryController {

    private final AlertsHistoryService alertsHistoryService;
    private final CurrentUserProvider currentUserProvider;

    public AlertsHistoryController(
            AlertsHistoryService alertsHistoryService,
            CurrentUserProvider currentUserProvider) {
        this.alertsHistoryService = alertsHistoryService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get SME alerts history snapshot with summary and log entries")
    public JsonNode getAlertsHistory() {
        var user = currentUserProvider.requireUser();
        return alertsHistoryService.getAlertsHistory(user, currentUserProvider.requireSegment());
    }
}
