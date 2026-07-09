package com.domoticore.teammanagement.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "team_invitations")
@Getter
@Setter
public class TeamInvitation {

    @Id
    private String id;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    @Column(name = "inviter_name", nullable = false)
    private String inviterName;

    @Column(name = "inviter_email", nullable = false)
    private String inviterEmail;

    @Column(name = "recipient_user_id")
    private Long recipientUserId;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "team_role", nullable = false)
    private String teamRole;

    @Column(name = "zones_json", nullable = false, columnDefinition = "TEXT")
    private String zonesJson;

    @Column(name = "invitation_type", nullable = false)
    private String invitationType;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_flag", nullable = false)
    private boolean read;

    @Column(name = "expires_at")
    private Instant expiresAt;

    protected TeamInvitation() {
    }

    public static TeamInvitation newEmpty() {
        return new TeamInvitation();
    }
}
