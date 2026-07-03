package com.domoticore.gateway.presentation;

import com.domoticore.gateway.application.GatewayService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.config.openapi.ApiAuthenticatedPatchResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gateways")
@Tag(name = "Gateway Management")
public class GatewayController {

    private final GatewayService gatewayService;
    private final CurrentUserProvider currentUserProvider;

    public GatewayController(GatewayService gatewayService, CurrentUserProvider currentUserProvider) {
        this.gatewayService = gatewayService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/current")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Get the linked gateway for the authenticated user")
    public ResponseEntity<JsonNode> getCurrentGateway() {
        JsonNode gateway = gatewayService.getGateway(currentUserProvider.requireUserId());
        if (gateway == null || gateway.isNull()) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(gateway);
    }

    @PostMapping("/link")
    @ResponseStatus(HttpStatus.OK)
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Link a gateway by MAC address or pairing code")
    public JsonNode linkGateway(@RequestBody ObjectNode body) {
        String macOrId = body.path("macOrId").asText("");
        String label = body.path("label").asText("");
        return gatewayService.linkGateway(currentUserProvider.requireUserId(), macOrId, label);
    }

    @DeleteMapping("/current")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unlink the current gateway and clear registered nodes")
    public void unlinkGateway() {
        gatewayService.unlinkGateway(currentUserProvider.requireUserId());
    }

    @GetMapping("/current/nodes")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "List nodes registered on the current gateway")
    public ArrayNode listNodes() {
        return gatewayService.listNodes(currentUserProvider.requireUserId());
    }

    @PostMapping("/current/nodes")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiAuthenticatedPatchResponses
    @Operation(summary = "Register a new device node on the gateway")
    public JsonNode registerNode(@RequestBody ObjectNode body) {
        return gatewayService.registerNode(
                currentUserProvider.requireUserId(),
                body.path("name").asText(""),
                body.path("type").asText("generic"));
    }
}
