package com.domoticore.iam.presentation.dto;

public record AuthResponse(String token, UserResponse user) {
}
