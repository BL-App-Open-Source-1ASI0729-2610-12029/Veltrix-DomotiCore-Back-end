package com.domoticore.settings.presentation;

import com.domoticore.settings.application.UserProfileService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.config.openapi.ApiAuthenticatedPatchResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-profile")
@Tag(name = "User Profile")
public class UserProfileController {

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
        return userProfileService.getProfile(currentUserProvider.requireUserId());
    }

    @PatchMapping("/me")
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Update settings profile for the authenticated user")
    public JsonNode patchMyProfile(@RequestBody JsonNode body) {
        return userProfileService.updateProfile(currentUserProvider.requireUserId(), body);
    }
}
