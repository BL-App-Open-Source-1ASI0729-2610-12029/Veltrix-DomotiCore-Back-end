package com.domoticore.teammanagement.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.infrastructure.persistence.jpa.UserRepository;
import com.domoticore.shared.infrastructure.security.UserDataScopeResolver;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TeamDataScopeService {

    private static final Set<String> TEAM_SHARED_COLLECTIONS = Set.of(
            "devices-overview",
            "device-details",
            "business-devices-overview");

    private final TeamMembershipService teamMembershipService;
    private final UserRepository userRepository;
    private final UserDataScopeResolver scopeResolver;

    public TeamDataScopeService(
            TeamMembershipService teamMembershipService,
            UserRepository userRepository,
            UserDataScopeResolver scopeResolver) {
        this.teamMembershipService = teamMembershipService;
        this.userRepository = userRepository;
        this.scopeResolver = scopeResolver;
    }

    public boolean isTeamSharedCollection(String collectionName) {
        return TEAM_SHARED_COLLECTIONS.contains(collectionName);
    }

    public TeamAccessContext resolve(User actor, String headerSegment, String collectionName) {
        String ownSegment = scopeResolver.resolveSegment(actor, headerSegment);
        if (!isTeamSharedCollection(collectionName)) {
            return TeamAccessContext.own(actor.getId(), ownSegment);
        }

        JsonNode snapshot = teamMembershipService.getMine(actor);
        JsonNode activeMembership = findActiveMembership(snapshot, collectionName);
        if (activeMembership == null) {
            return TeamAccessContext.own(actor.getId(), ownSegment);
        }

        Long inviterUserId = readInviterUserId(activeMembership);
        if (inviterUserId == null || inviterUserId <= 0) {
            return TeamAccessContext.own(actor.getId(), ownSegment);
        }

        String inviterSegment = resolveInviterSegment(activeMembership, inviterUserId);
        List<String> zones = readZones(activeMembership);
        String teamRole = activeMembership.path("teamRole").asText("viewer");

        return new TeamAccessContext(inviterUserId, inviterSegment, true, zones, teamRole);
    }

    private JsonNode findActiveMembership(JsonNode snapshot, String collectionName) {
        if (!snapshot.has("memberships") || !snapshot.get("memberships").isArray()) {
            return null;
        }

        String requiredSegment = requiredSegmentForCollection(collectionName);
        JsonNode globalFallback = null;
        JsonNode segmentFallback = null;

        for (JsonNode membership : snapshot.get("memberships")) {
            if (!"active".equalsIgnoreCase(membership.path("status").asText(""))) {
                continue;
            }

            String inviterSegment = resolveInviterSegment(
                    membership,
                    readInviterUserId(membership));

            if (TeamZoneAccess.hasGlobalAccess(readZones(membership))) {
                globalFallback = membership;
            }
            if (requiredSegment.equals(inviterSegment)) {
                segmentFallback = membership;
            }
        }

        if (segmentFallback != null) {
            return segmentFallback;
        }
        return globalFallback;
    }

    private String requiredSegmentForCollection(String collectionName) {
        return "business-devices-overview".equals(collectionName) ? "small-business" : "smart-home";
    }

    private Long readInviterUserId(JsonNode membership) {
        JsonNode node = membership.get("inviterUserId");
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.longValue();
        }
        String text = node.asText("").trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveInviterSegment(JsonNode membership, Long inviterUserId) {
        String stored = membership.path("inviterSegment").asText("");
        if (!stored.isBlank()) {
            return stored;
        }
        if (inviterUserId == null || inviterUserId <= 0) {
            return "smart-home";
        }
        return userRepository.findById(inviterUserId)
                .map(inviter -> scopeResolver.resolveSegment(inviter, null))
                .orElse("smart-home");
    }

    private List<String> readZones(JsonNode membership) {
        List<String> zones = new ArrayList<>();
        if (!membership.has("zones") || !membership.get("zones").isArray()) {
            zones.add("global");
            return zones;
        }
        membership.get("zones").forEach(zone -> zones.add(zone.asText("global")));
        if (zones.isEmpty()) {
            zones.add("global");
        }
        return zones;
    }
}
