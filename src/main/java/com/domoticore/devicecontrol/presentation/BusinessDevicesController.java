package com.domoticore.devicecontrol.presentation;

import com.domoticore.devicecontrol.application.BusinessDevicesService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-devices")
@Tag(name = "Business Devices")
public class BusinessDevicesController {

    private final BusinessDevicesService businessDevicesService;
    private final CurrentUserProvider currentUserProvider;

    public BusinessDevicesController(
            BusinessDevicesService businessDevicesService,
            CurrentUserProvider currentUserProvider) {
        this.businessDevicesService = businessDevicesService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/overview")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get SME business devices overview by zones")
    public JsonNode getOverview() {
        var user = currentUserProvider.requireUser();
        return businessDevicesService.getOverview(user, currentUserProvider.requireSegment());
    }

    @PatchMapping("/overview")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Update SME business devices overview")
    public JsonNode patchOverview(@RequestBody ObjectNode body) {
        var user = currentUserProvider.requireUser();
        return businessDevicesService.patchOverview(user, currentUserProvider.requireSegment(), body);
    }
}
