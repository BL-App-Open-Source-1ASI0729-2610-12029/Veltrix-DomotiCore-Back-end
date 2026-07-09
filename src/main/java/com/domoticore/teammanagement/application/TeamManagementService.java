package com.domoticore.teammanagement.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserScopedJsonResourceService;
import com.domoticore.shared.security.PlatformPermission;
import com.domoticore.shared.security.RolePermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class TeamManagementService {

    private static final String COLLECTION = "team-management";
    private static final String TEMPLATE_ID = "default";

    private final UserScopedJsonResourceService scopedJsonResourceService;
    private final RolePermissionService rolePermissionService;

    public TeamManagementService(
            UserScopedJsonResourceService scopedJsonResourceService,
            RolePermissionService rolePermissionService) {
        this.scopedJsonResourceService = scopedJsonResourceService;
        this.rolePermissionService = rolePermissionService;
    }

    public JsonNode getSnapshot(Long userId) {
        return scopedJsonResourceService.getOrCreateFromTemplate(COLLECTION, userId, TEMPLATE_ID);
    }

    @Transactional
    public JsonNode updateSnapshot(User user, JsonNode patch) {
        validateMemberChanges(user, patch);
        return scopedJsonResourceService.patchFromTemplate(COLLECTION, user.getId(), TEMPLATE_ID, patch);
    }

    private void validateMemberChanges(User user, JsonNode patch) {
        if (!patch.has("members") || !patch.get("members").isArray()) {
            rolePermissionService.require(user, PlatformPermission.TEAM_MANAGE);
            return;
        }

        JsonNode current = getSnapshot(user.getId());
        Set<String> before = memberIds(current.path("members"));
        Set<String> after = memberIds(patch.get("members"));

        boolean invited = after.size() > before.size() || after.stream().anyMatch(id -> !before.contains(id));
        boolean deleted = before.size() > after.size() || before.stream().anyMatch(id -> !after.contains(id));

        if (invited) {
            rolePermissionService.require(user, PlatformPermission.TEAM_INVITE);
        }
        if (deleted) {
            rolePermissionService.require(user, PlatformPermission.TEAM_DELETE);
        }
        if (!invited && !deleted) {
            rolePermissionService.require(user, PlatformPermission.TEAM_MANAGE);
        }
    }

    private Set<String> memberIds(JsonNode members) {
        Set<String> ids = new HashSet<>();
        if (!members.isArray()) {
            return ids;
        }
        for (JsonNode member : members) {
            ids.add(member.path("id").asText());
        }
        return ids;
    }
}
