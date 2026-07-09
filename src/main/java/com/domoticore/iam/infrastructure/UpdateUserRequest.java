package com.domoticore.iam.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateUserRequest")
public record UpdateUserRequest(
        String name,
        String email,
        String role,
        String avatar,
        String accountType,
        Boolean onboardingCompleted
) {
}
