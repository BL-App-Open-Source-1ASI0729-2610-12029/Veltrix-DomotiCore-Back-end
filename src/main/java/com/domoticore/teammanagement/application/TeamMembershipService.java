package com.domoticore.teammanagement.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.domoticore.teammanagement.domain.model.TeamInvitation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class TeamMembershipService {

    private static final String TEAM_MANAGEMENT_COLLECTION = "team-management";
    private static final String TEAM_MEMBERSHIP_COLLECTION = "team-membership";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;
    private final ObjectMapper objectMapper;

    public TeamMembershipService(
            UserScopedJsonResourceService scopedJsonResourceService,
            ObjectMapper objectMapper) {
        this.scopedJsonResourceService = scopedJsonResourceService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public JsonNode getMine(User user) {
        return scopedJsonResourceService.getOrCreateFromTemplate(
                TEAM_MEMBERSHIP_COLLECTION,
                user.getId(),
                TEMPLATE_ID);
    }

    @Transactional
    public void activateAcceptedInvitation(User invitee, TeamInvitation invitation) {
        if (invitation.getRecipientUserId() == null) {
            invitation.setRecipientUserId(invitee.getId());
        }

        activateInviterMember(invitation, invitee);
        upsertInviteeMembership(invitee, invitation);
    }

    private void activateInviterMember(TeamInvitation invitation, User invitee) {
        JsonNode snapshot = scopedJsonResourceService.getOrCreateFromTemplate(
                TEAM_MANAGEMENT_COLLECTION,
                invitation.getInviterUserId(),
                TEMPLATE_ID);

        if (!snapshot.has("members") || !snapshot.get("members").isArray()) {
            return;
        }

        ArrayNode members = objectMapper.createArrayNode();
        boolean updated = false;

        for (JsonNode memberNode : snapshot.get("members")) {
            ObjectNode member = memberNode.deepCopy();
            String email = member.path("email").asText("").trim().toLowerCase(Locale.ROOT);
            String linkedUserId = member.path("linkedUserId").asText("");
            boolean matchesEmail = email.equals(invitation.getRecipientEmail().toLowerCase(Locale.ROOT));
            boolean matchesUser = linkedUserId.equals(String.valueOf(invitee.getId()));

            if (matchesEmail || matchesUser) {
                member.put("tab", "all");
                member.put("status", "active");
                member.put("linkedUserId", invitee.getId());
                if (member.path("name").asText("").isBlank()) {
                    member.put("name", invitation.getMemberName());
                }
                updated = true;
            }
            members.add(member);
        }

        if (!updated) {
            ObjectNode pendingMember = objectMapper.createObjectNode();
            pendingMember.put("id", "member-" + invitee.getId());
            pendingMember.put("initials", initials(invitation.getMemberName()));
            pendingMember.put("name", invitation.getMemberName());
            pendingMember.put("email", invitation.getRecipientEmail());
            pendingMember.put("role", invitation.getTeamRole());
            pendingMember.set("zones", readZonesNode(invitation.getZonesJson()));
            pendingMember.put("status", "active");
            pendingMember.put("tab", "all");
            pendingMember.put("linkedUserId", invitee.getId());
            members.insert(0, pendingMember);
        }

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("members", members);
        scopedJsonResourceService.patchFromTemplate(
                TEAM_MANAGEMENT_COLLECTION,
                invitation.getInviterUserId(),
                TEMPLATE_ID,
                patch);
    }

    private void upsertInviteeMembership(User invitee, TeamInvitation invitation) {
        JsonNode current = scopedJsonResourceService.getOrCreateFromTemplate(
                TEAM_MEMBERSHIP_COLLECTION,
                invitee.getId(),
                TEMPLATE_ID);

        ArrayNode memberships = objectMapper.createArrayNode();
        if (current.has("memberships") && current.get("memberships").isArray()) {
            current.get("memberships").forEach(memberships::add);
        }

        ObjectNode entry = objectMapper.createObjectNode();
        entry.put("invitationId", invitation.getId());
        entry.put("inviterUserId", invitation.getInviterUserId());
        entry.put("inviterName", invitation.getInviterName());
        entry.put("teamRole", invitation.getTeamRole());
        entry.set("zones", readZonesNode(invitation.getZonesJson()));
        entry.put("status", "active");

        ArrayNode next = objectMapper.createArrayNode();
        boolean replaced = false;
        for (JsonNode existing : memberships) {
            if (invitation.getId().equals(existing.path("invitationId").asText())) {
                next.add(entry);
                replaced = true;
            } else {
                next.add(existing);
            }
        }
        if (!replaced) {
            next.insert(0, entry);
        }

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("memberships", next);
        scopedJsonResourceService.patchFromTemplate(
                TEAM_MEMBERSHIP_COLLECTION,
                invitee.getId(),
                TEMPLATE_ID,
                patch);
    }

    private ArrayNode readZonesNode(String zonesJson) {
        if (zonesJson == null || zonesJson.isBlank()) {
            return objectMapper.createArrayNode().add("global");
        }
        try {
            JsonNode parsed = objectMapper.readTree(zonesJson);
            if (parsed.isArray()) {
                return (ArrayNode) parsed;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return objectMapper.createArrayNode().add("global");
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "TM";
        }
        String value = name.trim();
        String[] parts = value.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                builder.append(part.charAt(0));
            }
            if (builder.length() >= 2) {
                break;
            }
        }
        return builder.length() == 0 ? "TM" : builder.toString().toUpperCase(Locale.ROOT);
    }
}
