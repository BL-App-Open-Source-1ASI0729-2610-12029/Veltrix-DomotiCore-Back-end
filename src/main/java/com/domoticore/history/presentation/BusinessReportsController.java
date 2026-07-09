package com.domoticore.history.presentation;

import com.domoticore.history.application.BusinessReportsService;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-reports")
@Tag(name = "Business Reports")
public class BusinessReportsController {

    private final BusinessReportsService businessReportsService;
    private final CurrentUserProvider currentUserProvider;

    public BusinessReportsController(
            BusinessReportsService businessReportsService,
            CurrentUserProvider currentUserProvider) {
        this.businessReportsService = businessReportsService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get SME business reports snapshot for a date range")
    public JsonNode getBusinessReports(
            @RequestParam(defaultValue = "thisMonth") String range) {
        var user = currentUserProvider.requireUser();
        return businessReportsService.getBusinessReports(user, currentUserProvider.requireSegment(), range);
    }
}
