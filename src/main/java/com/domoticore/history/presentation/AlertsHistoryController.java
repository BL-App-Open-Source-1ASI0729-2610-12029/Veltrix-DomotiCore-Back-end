package com.domoticore.history.presentation;

import com.domoticore.history.application.AlertsHistoryService;
import com.domoticore.shared.config.openapi.ApiGetByIdResponses;
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

    public AlertsHistoryController(AlertsHistoryService alertsHistoryService) {
        this.alertsHistoryService = alertsHistoryService;
    }

    @GetMapping
    @ApiGetByIdResponses
    @Operation(summary = "Get SME alerts history snapshot with summary and log entries")
    public JsonNode getAlertsHistory() {
        return alertsHistoryService.getAlertsHistory();
    }
}
