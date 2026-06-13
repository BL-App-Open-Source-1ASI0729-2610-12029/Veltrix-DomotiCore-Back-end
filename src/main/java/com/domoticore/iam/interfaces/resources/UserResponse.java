package com.domoticore.iam.interfaces.resources;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.valueobjects.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "UserResponse")
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
