package com.domoticore.teammanagement.infrastructure;

import java.util.List;

public record TeamInvitationResponse(
        String id,
        Long recipientUserId,
        String recipientEmail,
        String inviterName,
        String inviterEmail,
        String memberName,
        String role,
        List<String> zones,
        String type,
        String createdAt,
        boolean read,
        String status,
        String acceptUrl) {
}
