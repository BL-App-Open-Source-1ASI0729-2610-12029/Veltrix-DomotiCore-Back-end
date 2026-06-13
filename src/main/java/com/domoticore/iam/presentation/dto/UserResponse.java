package com.domoticore.iam.presentation.dto;

import com.domoticore.iam.domain.AccountType;
import com.domoticore.iam.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        String avatar,
        String accountType,
        Boolean onboardingCompleted
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatar(),
                user.getAccountType() != null ? user.getAccountType().toJson() : null,
                user.getOnboardingCompleted()
        );
    }
}
