package com.domoticore.smeoperations.presentation;

import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.smeoperations.application.OperationsHubService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations-hub")
@Tag(name = "SME Operations Hub")
public class OperationsHubController {

    private final OperationsHubService operationsHubService;
    private final CurrentUserProvider currentUserProvider;

    public OperationsHubController(
            OperationsHubService operationsHubService,
            CurrentUserProvider currentUserProvider) {
        this.operationsHubService = operationsHubService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/snapshot")
    @Operation(summary = "Get operations hub KPI snapshot for a date range")
    public JsonNode getSnapshot(
            @RequestParam(defaultValue = "thisMonth") String range) {
        return operationsHubService.getSnapshot(currentUserProvider.requireUserId(), range);
    }
}
