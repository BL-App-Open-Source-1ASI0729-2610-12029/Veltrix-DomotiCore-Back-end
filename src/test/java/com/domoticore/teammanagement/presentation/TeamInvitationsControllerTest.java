package com.domoticore.teammanagement.presentation;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.JwtAuthenticationFilter;
import com.domoticore.shared.infrastructure.security.JwtService;
import com.domoticore.teammanagement.application.TeamInvitationService;
import com.domoticore.teammanagement.infrastructure.TeamInvitationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamInvitationsController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamInvitationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamInvitationService teamInvitationService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        when(currentUserProvider.requireUser()).thenReturn(User.newEmpty());
    }

    @Test
    void sendInvitationCreatesRecord() throws Exception {
        TeamInvitationResponse response = new TeamInvitationResponse(
                "inv-1",
                3L,
                "mod@domoticore.local",
                "Admin",
                "admin@domoticore.local",
                "Moderator Demo",
                "manager",
                List.of("hq"),
                "team_invite",
                "2026-07-09T12:00:00Z",
                false,
                "pending",
                "http://localhost:4200/auth/login?invite=token");

        when(teamInvitationService.sendInvitation(any(User.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/team-invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientEmail": "mod@domoticore.local",
                                  "memberName": "Moderator Demo",
                                  "role": "manager",
                                  "zones": ["hq"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("inv-1"))
                .andExpect(jsonPath("$.recipientEmail").value("mod@domoticore.local"));

        verify(teamInvitationService).sendInvitation(any(User.class), any());
    }

    @Test
    void listMineReturnsRecipientInvitations() throws Exception {
        TeamInvitationResponse response = new TeamInvitationResponse(
                "inv-2",
                null,
                "home@domoticore.local",
                "Admin",
                "admin@domoticore.local",
                "Home Demo",
                "viewer",
                List.of("global"),
                "team_invite",
                "2026-07-09T12:00:00Z",
                false,
                "pending",
                "http://localhost:4200/auth/login?invite=token2");

        when(teamInvitationService.listMine(any(User.class))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/team-invitations/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipientEmail").value("home@domoticore.local"));
    }
}
