package com.domoticore.teammanagement.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamManagementService {

    private static final String COLLECTION = "team-management";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;

    public TeamManagementService(UserScopedJsonResourceService scopedJsonResourceService) {
        this.scopedJsonResourceService = scopedJsonResourceService;
    }

    public JsonNode getSnapshot(Long userId) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
    }

    @Transactional
    public JsonNode updateSnapshot(Long userId, JsonNode patch) {
        return scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
    }
}
