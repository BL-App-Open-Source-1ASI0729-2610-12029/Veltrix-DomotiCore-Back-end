package com.domoticore.history.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BusinessReportsService {

    private static final String COLLECTION = "business-reports";
    private static final Set<String> ALLOWED_RANGES = Set.of("thisMonth", "lastMonth", "thisQuarter");
    private static final String DEFAULT_RANGE = "thisMonth";

    private final UserCollectionAccessService userCollectionAccessService;

    public BusinessReportsService(UserCollectionAccessService userCollectionAccessService) {
        this.userCollectionAccessService = userCollectionAccessService;
    }

    public JsonNode getBusinessReports(User user, String segment, String range) {
        String resolvedRange = range == null || range.isBlank() ? DEFAULT_RANGE : range.trim();
        if (!ALLOWED_RANGES.contains(resolvedRange)) {
            throw new IllegalArgumentException("Invalid range: " + range);
        }
        return userCollectionAccessService.getSingleton(user, segment, COLLECTION, resolvedRange);
    }
}
