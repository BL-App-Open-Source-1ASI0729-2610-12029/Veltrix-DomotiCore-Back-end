package com.domoticore.iam.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest")
public record RegisterRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
