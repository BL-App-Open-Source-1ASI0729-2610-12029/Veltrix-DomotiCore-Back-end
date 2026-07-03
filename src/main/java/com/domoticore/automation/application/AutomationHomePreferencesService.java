package com.domoticore.automation.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutomationHomePreferencesService {

    private static final String COLLECTION = "automation-home-preferences";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;

    public AutomationHomePreferencesService(UserScopedJsonResourceService scopedJsonResourceService) {
        this.scopedJsonResourceService = scopedJsonResourceService;
    }

    public JsonNode getPreferences(Long userId) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
    }

    @Transactional
    public JsonNode updatePreferences(Long userId, JsonNode patch) {
        return scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
    }
}
