package com.domoticore.integrations.presentation;

import com.domoticore.integrations.application.BusinessProfileService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.config.openapi.ApiAuthenticatedPatchResponses;
import com.domoticore.shared.config.openapi.ApiPostActionResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.shared.security.PlatformPermission;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-profile")
@Tag(name = "Business Profile")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;
    private final CurrentUserProvider currentUserProvider;

    public BusinessProfileController(
            BusinessProfileService businessProfileService,
            CurrentUserProvider currentUserProvider) {
        this.businessProfileService = businessProfileService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get business profile and integration settings")
    public JsonNode getBusinessProfile() {
        return businessProfileService.getProfile(currentUserProvider.requireUserId());
    }

    @PatchMapping
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Partially update business profile")
    public JsonNode patchBusinessProfile(@RequestBody JsonNode body) {
        currentUserProvider.requirePermission(PlatformPermission.BUSINESS_PROFILE);
        return businessProfileService.updateProfile(currentUserProvider.requireUserId(), body);
    }

    @PostMapping("/regenerate-api-key")
    @ApiPostActionResponses
    @Operation(summary = "Regenerate developer API key for external integrations")
    public JsonNode regenerateApiKey() {
        currentUserProvider.requirePermission(PlatformPermission.BUSINESS_PROFILE);
        return businessProfileService.regenerateApiKey(currentUserProvider.requireUserId());
    }
}
