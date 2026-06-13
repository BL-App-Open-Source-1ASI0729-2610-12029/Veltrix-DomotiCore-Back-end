package com.domoticore.smeoperations.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class OperationsHubService {

    private static final String COLLECTION = "operations-hub-snapshot";

    private final UserScopedJsonResourceService scopedJsonResourceService;

    public OperationsHubService(UserScopedJsonResourceService scopedJsonResourceService) {
        this.scopedJsonResourceService = scopedJsonResourceService;
    }

    public JsonNode getSnapshot(Long userId, String range) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, range, range);
    }
}
