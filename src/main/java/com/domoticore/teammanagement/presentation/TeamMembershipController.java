package com.domoticore.teammanagement.presentation;

import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.teammanagement.application.TeamMembershipService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/team-membership")
@Tag(name = "Team Membership")
public class TeamMembershipController {

    private final TeamMembershipService teamMembershipService;
    private final CurrentUserProvider currentUserProvider;

    public TeamMembershipController(
            TeamMembershipService teamMembershipService,
            CurrentUserProvider currentUserProvider) {
        this.teamMembershipService = teamMembershipService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/mine")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get active team memberships for the current user")
    public JsonNode getMine() {
        return teamMembershipService.getMine(currentUserProvider.requireUser());
    }
}
