package com.domoticore.teammanagement.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.valueobjects.Email;
import com.domoticore.iam.infrastructure.persistence.jpa.UserRepository;
import com.domoticore.shared.domain.model.ForbiddenException;
import com.domoticore.shared.infrastructure.config.DomotiCoreMailProperties;
import com.domoticore.shared.infrastructure.security.PlatformPermission;
import com.domoticore.shared.infrastructure.security.RolePermissionService;
import com.domoticore.teammanagement.domain.model.TeamInvitation;
import com.domoticore.teammanagement.infrastructure.SendTeamInvitationRequest;
import com.domoticore.teammanagement.infrastructure.TeamInvitationResponse;
import com.domoticore.teammanagement.infrastructure.persistence.jpa.TeamInvitationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class TeamInvitationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamInvitationService.class);
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACCEPTED = "accepted";
    private static final String STATUS_DECLINED = "declined";

    private final TeamInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final RolePermissionService rolePermissionService;
    private final ObjectMapper objectMapper;
    private final DomotiCoreMailProperties mailProperties;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final TeamMembershipService teamMembershipService;

    public TeamInvitationService(
            TeamInvitationRepository invitationRepository,
            UserRepository userRepository,
            RolePermissionService rolePermissionService,
            ObjectMapper objectMapper,
            DomotiCoreMailProperties mailProperties,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            TeamMembershipService teamMembershipService) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.rolePermissionService = rolePermissionService;
        this.objectMapper = objectMapper;
        this.mailProperties = mailProperties;
        this.mailSenderProvider = mailSenderProvider;
        this.teamMembershipService = teamMembershipService;
    }

    @Transactional
    public TeamInvitationResponse sendInvitation(User inviter, SendTeamInvitationRequest request) {
        rolePermissionService.require(inviter, PlatformPermission.TEAM_INVITE);

        String recipientEmail = request.recipientEmail().trim().toLowerCase(Locale.ROOT);
        if (recipientEmail.equalsIgnoreCase(inviter.getEmail())) {
            throw new IllegalArgumentException("team.invitation.error.selfInvite");
        }

        Long recipientUserId = request.recipientUserId();
        if (recipientUserId == null) {
            recipientUserId = userRepository.findByEmailAddress(new Email(recipientEmail))
                    .map(User::getId)
                    .orElse(null);
        }

        String invitationType = normalizeType(request.type());
        TeamInvitation invitation = TeamInvitation.newEmpty();
        invitation.setId("inv-" + System.currentTimeMillis());
        invitation.setInviterUserId(inviter.getId());
        invitation.setInviterName(inviter.getName());
        invitation.setInviterEmail(inviter.getEmail());
        invitation.setRecipientUserId(recipientUserId);
        invitation.setRecipientEmail(recipientEmail);
        invitation.setMemberName(request.memberName().trim());
        invitation.setTeamRole(request.role().trim().toLowerCase(Locale.ROOT));
        invitation.setZonesJson(writeZones(request.zones()));
        invitation.setInvitationType(invitationType);
        invitation.setStatus(STATUS_PENDING);
        invitation.setToken(UUID.randomUUID().toString().replace("-", ""));
        invitation.setCreatedAt(Instant.now());
        invitation.setRead(false);
        invitation.setExpiresAt(Instant.now().plus(14, ChronoUnit.DAYS));

        TeamInvitation saved = invitationRepository.save(invitation);
        sendInvitationEmail(saved, request.zones());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamInvitationResponse> listMine(User user) {
        Map<String, TeamInvitation> byId = new LinkedHashMap<>();

        invitationRepository.findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(user.getEmail())
                .forEach(invitation -> byId.put(invitation.getId(), invitation));

        invitationRepository.findByRecipientUserIdOrderByCreatedAtDesc(user.getId())
                .forEach(invitation -> byId.put(invitation.getId(), invitation));

        return byId.values().stream()
                .sorted(Comparator.comparing(TeamInvitation::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeamInvitationResponse> listSent(User user) {
        rolePermissionService.require(user, PlatformPermission.TEAM_INVITE);
        return invitationRepository.findByInviterUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TeamInvitationResponse markRead(User user, String invitationId) {
        TeamInvitation invitation = requireRecipientInvitation(user, invitationId);
        invitation.setRead(true);
        return toResponse(invitationRepository.save(invitation));
    }

    @Transactional(readOnly = true)
    public TeamInvitationResponse findByTokenForUser(User user, String token) {
        TeamInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("team.invitation.error.notFound"));

        boolean matchesEmail = invitation.getRecipientEmail().equalsIgnoreCase(user.getEmail());
        boolean matchesUserId = invitation.getRecipientUserId() != null
                && invitation.getRecipientUserId().equals(user.getId());

        if (!matchesEmail && !matchesUserId) {
            throw new ForbiddenException("team.invitation.error.forbidden");
        }

        return toResponse(invitation);
    }

    @Transactional
    public TeamInvitationResponse acceptByToken(User user, String token) {
        TeamInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("team.invitation.error.notFound"));
        return accept(user, invitation.getId());
    }

    @Transactional
    public TeamInvitationResponse accept(User user, String invitationId) {
        TeamInvitation invitation = requireRecipientInvitation(user, invitationId);
        assertPending(invitation);
        invitation.setStatus(STATUS_ACCEPTED);
        invitation.setRead(true);
        if (invitation.getRecipientUserId() == null) {
            invitation.setRecipientUserId(user.getId());
        }
        TeamInvitation saved = invitationRepository.save(invitation);
        teamMembershipService.activateAcceptedInvitation(user, saved);
        return toResponse(saved);
    }

    @Transactional
    public TeamInvitationResponse decline(User user, String invitationId) {
        TeamInvitation invitation = requireRecipientInvitation(user, invitationId);
        assertPending(invitation);
        invitation.setStatus(STATUS_DECLINED);
        invitation.setRead(true);
        return toResponse(invitationRepository.save(invitation));
    }

    @Transactional
    public TeamInvitationResponse resend(User inviter, String invitationId) {
        rolePermissionService.require(inviter, PlatformPermission.TEAM_INVITE);
        TeamInvitation existing = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("team.invitation.error.notFound"));

        if (!existing.getInviterUserId().equals(inviter.getId())) {
            throw new ForbiddenException("team.invitation.error.forbidden");
        }

        SendTeamInvitationRequest request = new SendTeamInvitationRequest(
                existing.getRecipientUserId(),
                existing.getRecipientEmail(),
                existing.getMemberName(),
                existing.getTeamRole(),
                readZones(existing.getZonesJson()),
                "team_invite_resent");

        return sendInvitation(inviter, request);
    }

    private TeamInvitation requireRecipientInvitation(User user, String invitationId) {
        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("team.invitation.error.notFound"));

        boolean matchesEmail = invitation.getRecipientEmail().equalsIgnoreCase(user.getEmail());
        boolean matchesUserId = invitation.getRecipientUserId() != null
                && invitation.getRecipientUserId().equals(user.getId());

        if (!matchesEmail && !matchesUserId) {
            throw new ForbiddenException("team.invitation.error.forbidden");
        }

        return invitation;
    }

    private void assertPending(TeamInvitation invitation) {
        if (!STATUS_PENDING.equals(invitation.getStatus())) {
            throw new IllegalArgumentException("team.invitation.error.notPending");
        }
        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("team.invitation.error.expired");
        }
    }

    private void sendInvitationEmail(TeamInvitation invitation, List<String> zones) {
        String acceptUrl = buildAcceptUrl(invitation.getToken());
        String zonesLabel = String.join(", ", zones);
        boolean resent = "team_invite_resent".equals(invitation.getInvitationType());
        String subject = resent
                ? "DomotiCore: team invitation reminder"
                : "DomotiCore: you've been invited to join the team";
        String html = buildInvitationHtml(invitation, zonesLabel, acceptUrl, resent);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (!mailProperties.isEnabled() || mailSender == null) {
            LOGGER.info(
                    "Invitation email skipped (mail disabled). To: {} | Subject: {} | Accept: {}",
                    invitation.getRecipientEmail(),
                    subject,
                    acceptUrl);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.getFrom());
            helper.setTo(invitation.getRecipientEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            LOGGER.info("Invitation email sent to {}", invitation.getRecipientEmail());
        } catch (MessagingException ex) {
            LOGGER.warn("Failed to send invitation email to {}: {}", invitation.getRecipientEmail(), ex.getMessage());
        }
    }

    private String buildInvitationHtml(
            TeamInvitation invitation,
            String zonesLabel,
            String acceptUrl,
            boolean resent) {
        String intro = resent
                ? "<p><strong>" + escape(invitation.getInviterName()) + "</strong> resent your team invitation.</p>"
                : "<p><strong>" + escape(invitation.getInviterName()) + "</strong> invited you to join their DomotiCore team.</p>";

        return """
                <html><body style="font-family:Segoe UI,Arial,sans-serif;color:#1f2937;">
                <h2 style="color:#3455d1;">DomotiCore Team Invitation</h2>
                %s
                <p><strong>Role:</strong> %s<br/>
                <strong>Zones:</strong> %s<br/>
                <strong>Invited as:</strong> %s</p>
                <p><a href="%s" style="display:inline-block;padding:12px 20px;background:#3455d1;color:#fff;text-decoration:none;border-radius:8px;">Accept invitation</a></p>
                <p style="font-size:12px;color:#6b7280;">This link expires in 14 days. If you did not expect this email, you can ignore it.</p>
                </body></html>
                """.formatted(
                intro,
                escape(formatRole(invitation.getTeamRole())),
                escape(zonesLabel),
                escape(invitation.getMemberName()),
                acceptUrl);
    }

    private String buildAcceptUrl(String token) {
        String base = mailProperties.getFrontendUrl().replaceAll("/$", "");
        return base + "/auth/login?invite=" + token;
    }

    private TeamInvitationResponse toResponse(TeamInvitation invitation) {
        return new TeamInvitationResponse(
                invitation.getId(),
                invitation.getRecipientUserId(),
                invitation.getRecipientEmail(),
                invitation.getInviterName(),
                invitation.getInviterEmail(),
                invitation.getMemberName(),
                invitation.getTeamRole(),
                readZones(invitation.getZonesJson()),
                invitation.getInvitationType(),
                invitation.getCreatedAt().toString(),
                invitation.isRead(),
                invitation.getStatus(),
                buildAcceptUrl(invitation.getToken()));
    }

    private String writeZones(List<String> zones) {
        try {
            return objectMapper.writeValueAsString(zones == null ? List.of() : zones);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid zones payload", ex);
        }
    }

    private List<String> readZones(String zonesJson) {
        if (zonesJson == null || zonesJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(zonesJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "team_invite";
        }
        return "team_invite_resent".equals(type) ? "team_invite_resent" : "team_invite";
    }

    private String formatRole(String role) {
        return switch (role.toLowerCase(Locale.ROOT)) {
            case "administrator" -> "Administrator";
            case "manager" -> "Manager";
            case "viewer" -> "Viewer";
            default -> role;
        };
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
