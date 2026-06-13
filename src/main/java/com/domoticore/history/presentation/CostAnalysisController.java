package com.domoticore.history.presentation;

import com.domoticore.history.application.CostAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cost-analysis")
@Tag(name = "Cost Analysis")
public class CostAnalysisController {

    private final CostAnalysisService costAnalysisService;

    public CostAnalysisController(CostAnalysisService costAnalysisService) {
        this.costAnalysisService = costAnalysisService;
    }

    @GetMapping
    @Operation(summary = "Get SME cost analysis and billing audit snapshot")
    public JsonNode getCostAnalysis() {
        return costAnalysisService.getCostAnalysis();
    }
}
