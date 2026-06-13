package com.domoticore.iam.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateUserRequest(
        String name,
        String email,
        String role,
        String avatar,
        String accountType,
        Boolean onboardingCompleted
) {
}
