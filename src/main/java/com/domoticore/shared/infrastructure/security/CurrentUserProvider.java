package com.domoticore.shared.infrastructure.security;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.domain.model.ForbiddenException;
import com.domoticore.shared.domain.model.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentUserProvider {

    private final UserDataScopeResolver scopeResolver;
    private final RolePermissionService rolePermissionService;

    public CurrentUserProvider(UserDataScopeResolver scopeResolver, RolePermissionService rolePermissionService) {
        this.scopeResolver = scopeResolver;
        this.rolePermissionService = rolePermissionService;
    }

    public User requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DomotiCoreUserDetails details) {
            return details.getUser();
        }
        throw new UnauthorizedException("Authentication required");
    }

    public Long requireUserId() {
        return requireUser().getId();
    }

    public String requireSegment() {
        User user = requireUser();
        return scopeResolver.resolveSegment(user, readSegmentHeader());
    }

    public void requirePermission(PlatformPermission permission) {
        rolePermissionService.require(requireUser(), permission);
    }

    private String readSegmentHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getHeader(UserDataScopeResolver.SEGMENT_HEADER);
    }
}
