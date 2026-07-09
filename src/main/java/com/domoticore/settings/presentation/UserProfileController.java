package com.domoticore.settings.presentation;

import com.domoticore.settings.application.UserProfileService;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedPatchResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.PlatformPermission;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/user-profile")
@Tag(name = "User Profile")
public class UserProfileController {

    private static final Set<String> SYSTEM_FIELDS = Set.of(
            "dataRetentionDays",
            "roleKey",
            "jobTitleKey");

    private final UserProfileService userProfileService;
    private final CurrentUserProvider currentUserProvider;

    public UserProfileController(
            UserProfileService userProfileService,
            CurrentUserProvider currentUserProvider) {
        this.userProfileService = userProfileService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/me")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get settings profile for the authenticated user")
    public JsonNode getMyProfile() {
        currentUserProvider.requireUserId();
        return userProfileService.getProfile(currentUserProvider.requireUserId());
    }

    @PatchMapping("/me")
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Update settings profile for the authenticated user")
    public JsonNode patchMyProfile(@RequestBody JsonNode body) {
        if (containsSystemField(body)) {
            currentUserProvider.requirePermission(PlatformPermission.SETTINGS_SYSTEM);
        } else if (containsSensitiveIntegrationField(body)) {
            currentUserProvider.requirePermission(PlatformPermission.SETTINGS_ACCESS);
        }
        return userProfileService.updateProfile(currentUserProvider.requireUserId(), body);
    }

    private boolean containsSystemField(JsonNode body) {
        Iterator<String> fields = body.fieldNames();
        while (fields.hasNext()) {
            if (SYSTEM_FIELDS.contains(fields.next())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSensitiveIntegrationField(JsonNode body) {
        Iterator<String> fields = body.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            if ("twoFactorEnabled".equals(field)
                    || "googleConnected".equals(field)
                    || "appleKitConnected".equals(field)
                    || "nestConnected".equals(field)) {
                return true;
            }
        }
        return false;
    }
}
