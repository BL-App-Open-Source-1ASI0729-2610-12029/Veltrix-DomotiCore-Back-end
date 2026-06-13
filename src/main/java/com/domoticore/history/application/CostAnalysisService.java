package com.domoticore.history.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class CostAnalysisService {

    private static final String COLLECTION = "cost-analysis";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;

    public CostAnalysisService(UserScopedJsonResourceService scopedJsonResourceService) {
        this.scopedJsonResourceService = scopedJsonResourceService;
    }

    public JsonNode getCostAnalysis(Long userId) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
    }
}
