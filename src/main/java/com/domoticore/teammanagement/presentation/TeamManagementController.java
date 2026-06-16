package com.domoticore.teammanagement.presentation;

import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.config.openapi.ApiAuthenticatedPatchResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.teammanagement.application.TeamManagementService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/team-management")
@Tag(name = "Team Management")
public class TeamManagementController {

    private final TeamManagementService teamManagementService;
    private final CurrentUserProvider currentUserProvider;

    public TeamManagementController(
            TeamManagementService teamManagementService,
            CurrentUserProvider currentUserProvider) {
        this.teamManagementService = teamManagementService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get team management snapshot (members, zones, summary)")
    public JsonNode getTeamManagement() {
        return teamManagementService.getSnapshot(currentUserProvider.requireUserId());
    }

    @PatchMapping
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Update team management snapshot")
    public JsonNode patchTeamManagement(@RequestBody JsonNode body) {
        return teamManagementService.updateSnapshot(currentUserProvider.requireUserId(), body);
    }
}
