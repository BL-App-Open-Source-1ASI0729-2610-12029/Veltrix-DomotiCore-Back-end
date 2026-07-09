package com.domoticore.iam.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse")
public record AuthResponse(String token, UserResponse user) {
}
