package com.domoticore.history.application;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BusinessReportsService {

    private static final String COLLECTION = "business-reports";
    private static final Set<String> ALLOWED_RANGES = Set.of("thisMonth", "lastMonth", "thisQuarter");
    private static final String DEFAULT_RANGE = "thisMonth";

    private final JsonResourceService jsonResourceService;

    public BusinessReportsService(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    public JsonNode getBusinessReports(String range) {
        String resolvedRange = range == null || range.isBlank() ? DEFAULT_RANGE : range.trim();
        if (!ALLOWED_RANGES.contains(resolvedRange)) {
            throw new IllegalArgumentException("Invalid range: " + range);
        }
        return jsonResourceService.findById(COLLECTION, resolvedRange);
    }
}
