package com.domoticore.automation.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private static final String TIMELINE = "automation-active-rule-timeline";
    private static final String SCENES = "automation-active-scenes";
    private static final String EVENTS = "automation-upcoming-events";
    private static final String SUGGESTION = "automation-smart-suggestion";

    private final JsonResourceService jsonResourceService;

    public AutomationOperationsController(JsonResourceService jsonResourceService) {
        this.jsonResourceService = jsonResourceService;
    }

    @GetMapping("/rules")
    @Operation(summary = "List SME automation rules")
    public List<JsonNode> getRules() {
        return jsonResourceService.findAll(RULES);
    }

    @PatchMapping("/rules/{id}")
    @Operation(summary = "Partially update an automation rule")
    public JsonNode patchRule(@PathVariable String id, @RequestBody JsonNode body) {
        return jsonResourceService.patch(RULES, id, body);
    }

    @PostMapping("/rules/{id}/toggle")
    @Operation(summary = "Toggle automation rule active state")
    public JsonNode toggleRule(@PathVariable String id) {
        return jsonResourceService.toggleBooleanField(RULES, id, "active");
    }

    @GetMapping("/group-schedules")
    @Operation(summary = "List automation group schedules")
    public List<JsonNode> getGroupSchedules() {
        return jsonResourceService.findAll(GROUP_SCHEDULES);
    }

    @GetMapping("/shutdown-protocol")
    @Operation(summary = "Get facility shutdown protocol")
    public JsonNode getShutdownProtocol() {
        return jsonResourceService.findSingleton(SHUTDOWN_PROTOCOL, "closing-time");
    }

    @GetMapping("/efficiency-insights")
    @Operation(summary = "Get automation efficiency insights")
    public JsonNode getEfficiencyInsights() {
        return jsonResourceService.findSingleton(EFFICIENCY, "default");
    }

    @GetMapping("/active-rule-timeline")
    @Operation(summary = "Get active rule timeline snapshot")
    public JsonNode getActiveRuleTimeline() {
        return jsonResourceService.findSingleton(TIMELINE, "default");
    }

    @GetMapping("/active-scenes")
    @Operation(summary = "List active automation scenes")
    public List<JsonNode> getActiveScenes() {
        return jsonResourceService.findAll(SCENES);
    }

    @PostMapping("/active-scenes/{id}/toggle")
    @Operation(summary = "Toggle automation scene")
    public JsonNode toggleScene(@PathVariable String id) {
        return jsonResourceService.toggleBooleanField(SCENES, id, "active");
    }

    @GetMapping("/upcoming-events")
    @Operation(summary = "List upcoming automation events")
    public List<JsonNode> getUpcomingEvents() {
        return jsonResourceService.findAll(EVENTS);
    }

    @PostMapping("/upcoming-events/{id}/toggle")
    @Operation(summary = "Toggle upcoming automation event")
    public JsonNode toggleUpcomingEvent(@PathVariable String id) {
        return jsonResourceService.toggleBooleanField(EVENTS, id, "active");
    }

    @GetMapping("/smart-suggestion")
    @Operation(summary = "Get smart automation suggestion")
    public JsonNode getSmartSuggestion() {
        return jsonResourceService.findSingleton(SUGGESTION, "default");
    }
}
