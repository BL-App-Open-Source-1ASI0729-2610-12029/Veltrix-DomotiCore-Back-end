package com.domoticore.history.presentation;

import com.domoticore.history.application.ActivityStreamService;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiDeleteResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiGetListResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiPatchMutationResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiPostCreateResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/activity-streams")
@Tag(name = "Activity Streams")
public class ActivityStreamsController {

    private final ActivityStreamService activityStreamService;
    private final CurrentUserProvider currentUserProvider;

    public ActivityStreamsController(
            ActivityStreamService activityStreamService,
            CurrentUserProvider currentUserProvider) {
        this.activityStreamService = activityStreamService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @ApiGetListResponses
    @Operation(summary = "List activity stream entries (filtered by role server-side)")
    public List<JsonNode> list() {
        var user = currentUserProvider.requireUser();
        return activityStreamService.list(user, currentUserProvider.requireSegment());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiPostCreateResponses
    @Operation(summary = "Create activity stream entry (actor stamped server-side)")
    public JsonNode create(@RequestBody JsonNode body) {
        var user = currentUserProvider.requireUser();
        return activityStreamService.create(user, currentUserProvider.requireSegment(), body);
    }

    @PatchMapping("/{id}")
    @ApiPatchMutationResponses
    @Operation(summary = "Partially update activity stream entry")
    public JsonNode patch(@PathVariable String id, @RequestBody JsonNode body) {
        var user = currentUserProvider.requireUser();
        return activityStreamService.patch(user, currentUserProvider.requireSegment(), id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiDeleteResponses
    @Operation(summary = "Delete activity stream entry")
    public void delete(@PathVariable String id) {
        var user = currentUserProvider.requireUser();
        activityStreamService.delete(user, currentUserProvider.requireSegment(), id);
    }
}
