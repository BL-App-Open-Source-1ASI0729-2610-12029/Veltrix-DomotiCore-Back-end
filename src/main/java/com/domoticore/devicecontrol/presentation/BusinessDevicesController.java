package com.domoticore.devicecontrol.presentation;

import com.domoticore.devicecontrol.application.BusinessDevicesService;
import com.domoticore.shared.config.openapi.ApiGetByIdResponses;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-devices")
@Tag(name = "Business Devices")
public class BusinessDevicesController {

    private final BusinessDevicesService businessDevicesService;

    public BusinessDevicesController(BusinessDevicesService businessDevicesService) {
        this.businessDevicesService = businessDevicesService;
    }

    @GetMapping("/overview")
    @ApiGetByIdResponses
    @Operation(summary = "Get SME business devices overview by zones")
    public JsonNode getOverview() {
        return businessDevicesService.getOverview();
    }
}
