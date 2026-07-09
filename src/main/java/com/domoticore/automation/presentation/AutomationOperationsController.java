package com.domoticore.automation.presentation;

import com.domoticore.automation.application.AutomationActionService;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiGetByIdResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiGetListResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiPostActionResponses;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/automation")
@Tag(name = "Automation Operations")
public class AutomationOperationsController {

    private static final String RULES = "automation-rules";
    private static final String GROUP_SCHEDULES = "automation-group-schedules";
    private static final String SHUTDOWN_PROTOCOL = "automation-shutdown-protocol";
    private static final String EFFICIENCY = "automation-efficiency-insights";
    private static final String SCENES = "automation-active-scenes";
    private static final String EVENTS = "automation-upcoming-events";
    private static final String SUGGESTION = "automation-smart-suggestion";
    private static final String TIMELINE = "automation-active-rule-timeline";

    private final UserCollectionAccessService userCollectionAccessService;
    private final AutomationActionService automationActionService;
    private final CurrentUserProvider currentUserProvider;

    public AutomationOperationsController(
            UserCollectionAccessService userCollectionAccessService,
            AutomationActionService automationActionService,
            CurrentUserProvider currentUserProvider) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.automationActionService = automationActionService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/rules")
    @ApiAuthenticatedGetResponses
    @ApiGetListResponses
    @Operation(summary = "List SME automation rules")
    public List<JsonNode> getRules() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.list(user, currentUserProvider.requireSegment(), RULES);
    }

    @PostMapping("/rules/{id}/toggle")
    @ApiPostActionResponses
    @Operation(summary = "Toggle automation rule active state")
    public JsonNode toggleRule(@PathVariable String id) {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.toggleBooleanField(
                user, currentUserProvider.requireSegment(), RULES, id, "active");
    }

    @PostMapping("/rules")
    @ApiPostActionResponses
    @Operation(summary = "Create a new automation rule")
    public JsonNode createRule(@RequestBody ObjectNode body) {
        var user = currentUserProvider.requireUser();
        return automationActionService.createRule(user, currentUserProvider.requireSegment(), body);
    }

    @PostMapping("/shutdown-protocol/steps/{stepId}/toggle")
    @ApiPostActionResponses
    @Operation(summary = "Toggle shutdown protocol step enabled state")
    public JsonNode toggleShutdownStep(@PathVariable String stepId) {
        var user = currentUserProvider.requireUser();
        return automationActionService.toggleShutdownStep(user, currentUserProvider.requireSegment(), stepId);
    }

    @PostMapping("/smart-suggestion/dismiss")
    @ApiPostActionResponses
    @Operation(summary = "Dismiss smart automation suggestion")
    public JsonNode dismissSmartSuggestion() {
        var user = currentUserProvider.requireUser();
        return automationActionService.dismissSmartSuggestion(user, currentUserProvider.requireSegment());
    }

    @GetMapping("/group-schedules")
    @ApiAuthenticatedGetResponses
    @ApiGetListResponses
    @Operation(summary = "List automation group schedules")
    public List<JsonNode> getGroupSchedules() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.list(user, currentUserProvider.requireSegment(), GROUP_SCHEDULES);
    }

    @GetMapping("/shutdown-protocol")
    @ApiAuthenticatedGetResponses
    @ApiGetByIdResponses
    @Operation(summary = "Get facility shutdown protocol")
    public JsonNode getShutdownProtocol() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.getSingleton(
                user, currentUserProvider.requireSegment(), SHUTDOWN_PROTOCOL, "closing-time");
    }

    @PutMapping("/shutdown-protocol")
    @ApiPostActionResponses
    @Operation(summary = "Update facility shutdown protocol")
    public JsonNode saveShutdownProtocol(@RequestBody ObjectNode body) {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.patch(
                user, currentUserProvider.requireSegment(), SHUTDOWN_PROTOCOL, "closing-time", body);
    }

    @GetMapping("/efficiency-insights")
    @ApiAuthenticatedGetResponses
    @ApiGetByIdResponses
    @Operation(summary = "Get automation efficiency insights")
    public JsonNode getEfficiencyInsights() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.getSingleton(
                user, currentUserProvider.requireSegment(), EFFICIENCY, "default");
    }

    @GetMapping("/active-scenes")
    @ApiAuthenticatedGetResponses
    @ApiGetListResponses
    @Operation(summary = "List active automation scenes")
    public List<JsonNode> getActiveScenes() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.list(user, currentUserProvider.requireSegment(), SCENES);
    }

    @PostMapping("/active-scenes/{id}/toggle")
    @ApiPostActionResponses
    @Operation(summary = "Toggle automation scene")
    public JsonNode toggleScene(@PathVariable String id) {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.toggleBooleanField(
                user, currentUserProvider.requireSegment(), SCENES, id, "active");
    }

    @GetMapping("/upcoming-events")
    @ApiAuthenticatedGetResponses
    @ApiGetListResponses
    @Operation(summary = "List upcoming automation events")
    public List<JsonNode> getUpcomingEvents() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.list(user, currentUserProvider.requireSegment(), EVENTS);
    }

    @PostMapping("/upcoming-events/{id}/toggle")
    @ApiPostActionResponses
    @Operation(summary = "Toggle upcoming automation event")
    public JsonNode toggleUpcomingEvent(@PathVariable String id) {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.toggleBooleanField(
                user, currentUserProvider.requireSegment(), EVENTS, id, "active");
    }

    @GetMapping("/smart-suggestion")
    @ApiAuthenticatedGetResponses
    @ApiGetByIdResponses
    @Operation(summary = "Get smart automation suggestion")
    public JsonNode getSmartSuggestion() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.getSingleton(
                user, currentUserProvider.requireSegment(), SUGGESTION, "default");
    }

    @GetMapping("/active-rule-timeline")
    @ApiAuthenticatedGetResponses
    @ApiGetByIdResponses
    @Operation(summary = "Get active automation rule timeline for SME operations")
    public JsonNode getActiveRuleTimeline() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.getSingleton(
                user, currentUserProvider.requireSegment(), TIMELINE, "default");
    }

    @PostMapping("/activate-eco-mode")
    @ApiPostActionResponses
    @Operation(summary = "Activate manual eco mode and reduce high-consumption devices")
    public JsonNode activateEcoMode() {
        var user = currentUserProvider.requireUser();
        return automationActionService.activateEcoMode(user, currentUserProvider.requireSegment());
    }

    @PostMapping("/scenes/{id}/execute")
    @ApiPostActionResponses
    @Operation(summary = "Execute an automation scene and apply device actions")
    public JsonNode executeScene(@PathVariable String id) {
        var user = currentUserProvider.requireUser();
        return automationActionService.executeScene(user, currentUserProvider.requireSegment(), id);
    }
}
