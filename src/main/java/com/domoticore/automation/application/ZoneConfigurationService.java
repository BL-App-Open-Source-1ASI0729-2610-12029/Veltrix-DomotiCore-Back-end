package com.domoticore.automation.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZoneConfigurationService {

    private static final String COLLECTION = "zone-configuration";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;

    public ZoneConfigurationService(UserScopedJsonResourceService scopedJsonResourceService) {
        this.scopedJsonResourceService = scopedJsonResourceService;
    }

    public JsonNode getConfiguration(Long userId) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
    }

    @Transactional
    public JsonNode updateConfiguration(Long userId, JsonNode patch) {
        return scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
    }
}
