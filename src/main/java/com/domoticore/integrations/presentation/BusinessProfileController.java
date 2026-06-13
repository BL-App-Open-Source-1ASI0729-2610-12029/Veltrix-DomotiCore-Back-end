package com.domoticore.integrations.presentation;

import com.domoticore.integrations.application.BusinessProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-profile")
@Tag(name = "Business Profile")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    public BusinessProfileController(BusinessProfileService businessProfileService) {
        this.businessProfileService = businessProfileService;
    }

    @GetMapping
    @Operation(summary = "Get business profile and integration settings")
    public JsonNode getBusinessProfile() {
        return businessProfileService.getProfile();
    }

    @PatchMapping
    @Operation(summary = "Partially update business profile")
    public JsonNode patchBusinessProfile(@RequestBody JsonNode body) {
        return businessProfileService.updateProfile(body);
    }
}
