package com.domoticore.integrations.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessProfileService {

    private static final String COLLECTION = "business-profile";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;

    public BusinessProfileService(UserScopedJsonResourceService scopedJsonResourceService) {
        this.scopedJsonResourceService = scopedJsonResourceService;
    }

    public JsonNode getProfile(Long userId) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
    }

    @Transactional
    public JsonNode updateProfile(Long userId, JsonNode patch) {
        return scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
    }
}
