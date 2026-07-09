CREATE TABLE team_invitations (
    id VARCHAR(64) PRIMARY KEY,
    inviter_user_id BIGINT NOT NULL,
    inviter_name VARCHAR(255) NOT NULL,
    inviter_email VARCHAR(255) NOT NULL,
    recipient_user_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    member_name VARCHAR(255) NOT NULL,
    team_role VARCHAR(64) NOT NULL,
    zones_json TEXT NOT NULL,
    invitation_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    token VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP,
    CONSTRAINT uk_team_invitations_token UNIQUE (token)
);

CREATE INDEX idx_team_invitations_recipient_email ON team_invitations (recipient_email);
CREATE INDEX idx_team_invitations_recipient_user_id ON team_invitations (recipient_user_id);
CREATE INDEX idx_team_invitations_inviter_user_id ON team_invitations (inviter_user_id);
