package com.domoticore.teammanagement.presentation;

import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedPatchResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiPostCreateResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.teammanagement.application.TeamInvitationService;
import com.domoticore.teammanagement.infrastructure.AcceptInvitationByTokenRequest;
import com.domoticore.teammanagement.infrastructure.SendTeamInvitationRequest;
import com.domoticore.teammanagement.infrastructure.TeamInvitationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team-invitations")
@Tag(name = "Team Invitations")
public class TeamInvitationsController {

    private final TeamInvitationService teamInvitationService;
    private final CurrentUserProvider currentUserProvider;

    public TeamInvitationsController(
            TeamInvitationService teamInvitationService,
            CurrentUserProvider currentUserProvider) {
        this.teamInvitationService = teamInvitationService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiPostCreateResponses
    @Operation(summary = "Send a team invitation (requires TEAM_INVITE)")
    public TeamInvitationResponse sendInvitation(@Valid @RequestBody SendTeamInvitationRequest request) {
        return teamInvitationService.sendInvitation(currentUserProvider.requireUser(), request);
    }

    @GetMapping("/mine")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "List invitations received by the current user")
    public List<TeamInvitationResponse> listMine() {
        return teamInvitationService.listMine(currentUserProvider.requireUser());
    }

    @GetMapping("/sent")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "List invitations sent by the current user")
    public List<TeamInvitationResponse> listSent() {
        return teamInvitationService.listSent(currentUserProvider.requireUser());
    }

    @GetMapping("/by-token/{token}")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Find a pending invitation by email token for the current user")
    public TeamInvitationResponse findByToken(@PathVariable String token) {
        return teamInvitationService.findByTokenForUser(currentUserProvider.requireUser(), token);
    }

    @PatchMapping("/{id}/read")
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Mark an invitation notification as read")
    public TeamInvitationResponse markRead(@PathVariable String id) {
        return teamInvitationService.markRead(currentUserProvider.requireUser(), id);
    }

    @PostMapping("/{id}/accept")
    @ApiPostCreateResponses
    @Operation(summary = "Accept a pending team invitation")
    public TeamInvitationResponse accept(@PathVariable String id) {
        return teamInvitationService.accept(currentUserProvider.requireUser(), id);
    }

    @PostMapping("/accept-by-token")
    @ApiPostCreateResponses
    @Operation(summary = "Accept a pending team invitation using the email token")
    public TeamInvitationResponse acceptByToken(@Valid @RequestBody AcceptInvitationByTokenRequest request) {
        return teamInvitationService.acceptByToken(currentUserProvider.requireUser(), request.token());
    }

    @PostMapping("/{id}/decline")
    @ApiPostCreateResponses
    @Operation(summary = "Decline a pending team invitation")
    public TeamInvitationResponse decline(@PathVariable String id) {
        return teamInvitationService.decline(currentUserProvider.requireUser(), id);
    }

    @PostMapping("/{id}/resend")
    @ApiPostCreateResponses
    @Operation(summary = "Resend a team invitation email")
    public TeamInvitationResponse resend(@PathVariable String id) {
        return teamInvitationService.resend(currentUserProvider.requireUser(), id);
    }
}
