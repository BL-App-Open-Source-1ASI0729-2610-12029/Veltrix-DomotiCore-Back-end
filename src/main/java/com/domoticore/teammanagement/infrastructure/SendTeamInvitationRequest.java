package com.domoticore.teammanagement.infrastructure;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SendTeamInvitationRequest(
        Long recipientUserId,
        @NotBlank @Email String recipientEmail,
        @NotBlank String memberName,
        @NotBlank String role,
        @NotEmpty List<String> zones,
        String type) {
}
