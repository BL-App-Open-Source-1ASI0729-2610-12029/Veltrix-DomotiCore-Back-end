package com.domoticore.iam.domain.model.commands;

public record UpdateUserCommand(
        Long userId,
        String name,
        String email,
        String role,
        String avatar,
        String accountType,
        Boolean onboardingCompleted
) {
}
