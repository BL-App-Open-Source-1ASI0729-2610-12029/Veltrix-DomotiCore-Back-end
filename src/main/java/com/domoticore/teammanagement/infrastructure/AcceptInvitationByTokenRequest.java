package com.domoticore.teammanagement.infrastructure;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationByTokenRequest(@NotBlank String token) {
}
