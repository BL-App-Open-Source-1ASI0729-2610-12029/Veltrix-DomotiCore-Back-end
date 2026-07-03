package com.domoticore.shared.security;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.valueobjects.AccountType;
import org.springframework.stereotype.Component;

@Component
public class UserDataScopeResolver {

    public static final String SEGMENT_HEADER = "X-DomotiCore-Segment";

    public String resolveSegment(User user, String headerSegment) {
        if (PlatformRole.from(user.getRole()) == PlatformRole.ADMIN) {
            if ("smart-home".equalsIgnoreCase(headerSegment)) {
                return "smart-home";
            }
            if ("small-business".equalsIgnoreCase(headerSegment)) {
                return "small-business";
            }
            return "smart-home";
        }

        if (user.getAccountType() == AccountType.SMALL_BUSINESS) {
            return "small-business";
        }
        return "smart-home";
    }

    public String scopePrefix(Long userId, String segment) {
        return userId + "-" + segment + "-";
    }
}
