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
        JsonNode activeMembership = findActiveMembership(snapshot);
        if (activeMembership == null) {
            return TeamAccessContext.own(actor.getId(), ownSegment);
        }

        Long inviterUserId = activeMembership.path("inviterUserId").asLong(0);
        if (inviterUserId <= 0) {
            return TeamAccessContext.own(actor.getId(), ownSegment);
        }

        String inviterSegment = activeMembership.path("inviterSegment").asText("");
        if (inviterSegment.isBlank()) {
            inviterSegment = userRepository.findById(inviterUserId)
                    .map(inviter -> scopeResolver.resolveSegment(inviter, null))
                    .orElse(ownSegment);
        }

        List<String> zones = readZones(activeMembership);
        String teamRole = activeMembership.path("teamRole").asText("viewer");

        if (!inviterSegment.equals(ownSegment)) {
            return TeamAccessContext.own(actor.getId(), ownSegment);
        }

        return new TeamAccessContext(inviterUserId, inviterSegment, true, zones, teamRole);
    }

    private JsonNode findActiveMembership(JsonNode snapshot) {
        if (!snapshot.has("memberships") || !snapshot.get("memberships").isArray()) {
            return null;
        }

        JsonNode fallback = null;
        for (JsonNode membership : snapshot.get("memberships")) {
            if (!"active".equalsIgnoreCase(membership.path("status").asText(""))) {
                continue;
            }
            if (TeamZoneAccess.hasGlobalAccess(readZones(membership))) {
                return membership;
            }
            fallback = membership;
        }
        return fallback;
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
