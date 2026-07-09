package com.domoticore.iam.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest")
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
