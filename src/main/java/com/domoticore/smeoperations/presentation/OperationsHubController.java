package com.domoticore.smeoperations.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations-hub")
@Tag(name = "SME Operations Hub")
public class OperationsHubController {

    private static final String COLLECTION = "operations-hub-snapshot";

    private final JsonResourceService jsonResourceService;

    public OperationsHubController(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    @GetMapping("/snapshot")
    @Operation(summary = "Get operations hub KPI snapshot for a date range")
    public JsonNode getSnapshot(
            @RequestParam(defaultValue = "thisMonth") String range) {
        return stripInternalId(jsonResourceService.findById(COLLECTION, range));
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
