package com.domoticore.teammanagement.infrastructure.persistence.jpa;

import com.domoticore.teammanagement.domain.model.TeamInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, String> {

    List<TeamInvitation> findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(String recipientEmail);

    List<TeamInvitation> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);

    List<TeamInvitation> findByInviterUserIdOrderByCreatedAtDesc(Long inviterUserId);

    Optional<TeamInvitation> findByToken(String token);
}
