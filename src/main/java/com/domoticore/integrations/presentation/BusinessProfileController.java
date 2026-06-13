package com.domoticore.integrations.presentation;

import com.domoticore.shared.application.JsonResourceService;
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

    private static final String COLLECTION = "business-profile";
    private static final String DEFAULT_ID = "default";

    private final JsonResourceService jsonResourceService;

    public BusinessProfileController(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    @GetMapping
    @Operation(summary = "Get business profile and integration settings")
    public JsonNode getBusinessProfile() {
        return stripInternalId(jsonResourceService.findSingleton(COLLECTION, DEFAULT_ID));
    }

    @PatchMapping
    @Operation(summary = "Partially update business profile")
    public JsonNode patchBusinessProfile(@RequestBody JsonNode body) {
        return stripInternalId(jsonResourceService.patch(COLLECTION, DEFAULT_ID, body));
    }

    private JsonNode stripInternalId(JsonNode payload) {
        if (payload instanceof com.fasterxml.jackson.databind.node.ObjectNode objectNode) {
            com.fasterxml.jackson.databind.node.ObjectNode copy = objectNode.deepCopy();
            copy.remove("id");
            return copy;
        }
        return payload;
    }
}
