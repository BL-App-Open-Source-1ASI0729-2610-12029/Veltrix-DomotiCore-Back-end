package com.domoticore.teammanagement.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/team-management")
@Tag(name = "Team Management")
public class TeamManagementController {

    private static final String COLLECTION = "team-management";
    private static final String DEFAULT_ID = "default";

    private final JsonResourceService jsonResourceService;

    public TeamManagementController(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    @GetMapping
    @Operation(summary = "Get team management snapshot (members, zones, summary)")
    public JsonNode getTeamManagement() {
        return stripInternalId(jsonResourceService.findSingleton(COLLECTION, DEFAULT_ID));
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
