package com.domoticore.gateway.application;

import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class GatewayService {

    private static final String COLLECTION = "gateway-state";
    private static final String TEMPLATE_ID = "default";
    private static final Pattern MAC_PATTERN = Pattern.compile("^([0-9A-F]{2}:){5}[0-9A-F]{2}$");

    private static final Set<String> DEMO_CODES = Set.of(
            "AA:BB:CC:DD:EE:01",
            "VELTRIX-GW-001",
            "VELTRIX-GW-DEMO"
    );

    private final UserScopedJsonResourceService scopedJsonResourceService;
    private final ObjectMapper objectMapper;

    public GatewayService(UserScopedJsonResourceService scopedJsonResourceService, ObjectMapper objectMapper) {
        this.scopedJsonResourceService = scopedJsonResourceService;
        this.objectMapper = objectMapper;
    }

    public JsonNode getGateway(Long userId) {
        JsonNode state = scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
        JsonNode gateway = state.get("gateway");
        if (gateway == null || gateway.isNull()) {
            return objectMapper.nullNode();
        }
        return gateway;
    }

    @Transactional
    public JsonNode linkGateway(Long userId, String macOrId, String label) {
        String normalized = macOrId == null ? "" : macOrId.trim().toUpperCase(Locale.ROOT);
        if (!isValidGatewayCode(normalized)) {
            throw new IllegalArgumentException("GATEWAY_NOT_DETECTED");
        }

        String now = Instant.now().toString();
        ObjectNode gateway = objectMapper.createObjectNode();
        gateway.put("id", "gw-primary");
        gateway.put("label", label == null || label.isBlank() ? "Veltrix Gateway" : label.trim());
        gateway.put("macOrId", normalized);
        gateway.put("status", "online");
        gateway.put("linkedAt", now);
        gateway.put("lastSeenAt", now);

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("gateway", gateway);
        scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
        return gateway;
    }

    @Transactional
    public void unlinkGateway(Long userId) {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.putNull("gateway");
        patch.set("nodes", objectMapper.createArrayNode());
        scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
    }

    public ArrayNode listNodes(Long userId) {
        JsonNode state = scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
        JsonNode nodes = state.get("nodes");
        if (nodes instanceof ArrayNode arrayNode) {
            return arrayNode;
        }
        return objectMapper.createArrayNode();
    }

    @Transactional
    public JsonNode registerNode(Long userId, String name, String type) {
        JsonNode state = scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
        JsonNode gateway = state.get("gateway");
        if (gateway == null || gateway.isNull()) {
            throw new IllegalArgumentException("GATEWAY_NOT_LINKED");
        }
        if (!"online".equals(gateway.path("status").asText())) {
            throw new IllegalArgumentException("GATEWAY_OFFLINE");
        }

        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isBlank()) {
            throw new IllegalArgumentException("NODE_NAME_REQUIRED");
        }

        ArrayNode nodes = listNodes(userId);
        for (JsonNode node : nodes) {
            if (node.path("name").asText("").equalsIgnoreCase(trimmedName)) {
                throw new IllegalArgumentException("NODE_ALREADY_EXISTS");
            }
        }

        String gatewayId = gateway.path("id").asText("gw-primary");
        String nodeType = type == null || type.isBlank() ? "generic" : type.trim();
        boolean canRegister = Pattern.compile(
                        "lamp|light|bulb|plug|sensor|switch|foco|luz|enchufe",
                        Pattern.CASE_INSENSITIVE)
                .matcher(trimmedName + " " + nodeType)
                .find();

        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", "node-" + System.currentTimeMillis());
        node.put("gatewayId", gatewayId);
        node.put("name", trimmedName);
        node.put("type", nodeType);
        node.put("status", canRegister ? "registered" : "failed");
        if (canRegister) {
            node.put("registeredAt", Instant.now().toString());
        } else {
            node.putNull("registeredAt");
        }

        ArrayNode updatedNodes = objectMapper.createArrayNode();
        updatedNodes.add(node);
        nodes.forEach(updatedNodes::add);

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("nodes", updatedNodes);
        scopedJsonResourceService.patchFromTemplate(COLLECTION, userId, TEMPLATE_ID, patch);
        return node;
    }

    private boolean isValidGatewayCode(String normalized) {
        return DEMO_CODES.contains(normalized) || MAC_PATTERN.matcher(normalized).matches();
    }
}
