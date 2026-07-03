package com.domoticore.shared.security;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class RolePermissionService {

    private static final Map<PlatformRole, Set<PlatformPermission>> PERMISSIONS = Map.of(
            PlatformRole.ADMIN, EnumSet.of(
                    PlatformPermission.SEGMENT_BOTH,
                    PlatformPermission.TEAM_MANAGE,
                    PlatformPermission.TEAM_INVITE,
                    PlatformPermission.TEAM_DELETE,
                    PlatformPermission.SETTINGS_ACCESS,
                    PlatformPermission.SETTINGS_SYSTEM,
                    PlatformPermission.SETTINGS_AUTHORIZED_USERS,
                    PlatformPermission.DEVICES_DELETE,
                    PlatformPermission.MAINTENANCE_REGISTER,
                    PlatformPermission.EXPORT_DATA,
                    PlatformPermission.GATEWAY_MANAGE,
                    PlatformPermission.INTEGRATIONS_MANAGE,
                    PlatformPermission.BUSINESS_PROFILE),
            PlatformRole.MODERATOR, EnumSet.of(
                    PlatformPermission.TEAM_MANAGE,
                    PlatformPermission.TEAM_INVITE,
                    PlatformPermission.SETTINGS_ACCESS,
                    PlatformPermission.SETTINGS_AUTHORIZED_USERS,
                    PlatformPermission.DEVICES_DELETE,
                    PlatformPermission.MAINTENANCE_REGISTER,
                    PlatformPermission.EXPORT_DATA,
                    PlatformPermission.GATEWAY_MANAGE,
                    PlatformPermission.INTEGRATIONS_MANAGE),
            PlatformRole.USER, EnumSet.noneOf(PlatformPermission.class));

    public PlatformRole roleOf(User user) {
        return PlatformRole.from(user.getRole());
    }

    public boolean can(User user, PlatformPermission permission) {
        return PERMISSIONS.getOrDefault(roleOf(user), EnumSet.noneOf(PlatformPermission.class))
                .contains(permission);
    }

    public void require(User user, PlatformPermission permission) {
        if (!can(user, permission)) {
            throw new ForbiddenException("iam.permission.denied:" + permission.name());
        }
    }
}
