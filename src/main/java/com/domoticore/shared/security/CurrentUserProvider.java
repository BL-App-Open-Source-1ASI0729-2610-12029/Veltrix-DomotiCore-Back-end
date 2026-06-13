package com.domoticore.shared.security;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

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
}
