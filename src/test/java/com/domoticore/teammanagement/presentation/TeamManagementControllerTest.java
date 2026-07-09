package com.domoticore.teammanagement.presentation;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.JwtAuthenticationFilter;
import com.domoticore.shared.infrastructure.security.JwtService;
import com.domoticore.teammanagement.application.TeamManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeamManagementService teamManagementService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        when(currentUserProvider.requireUserId()).thenReturn(7L);
        when(currentUserProvider.requireUser()).thenReturn(new User());
        doNothing().when(currentUserProvider).requirePermission(any());
    }

    @Test
    void getTeamManagementReturnsAuthenticatedUserPayload() throws Exception {
        ObjectNode team = objectMapper.createObjectNode();
        team.put("totalMembers", 24);

        when(teamManagementService.getSnapshot(7L)).thenReturn(team);

        mockMvc.perform(get("/api/v1/team-management"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(24));
    }

    @Test
    void patchTeamManagementUpdatesAuthenticatedUserPayload() throws Exception {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("totalMembers", 25);

        ObjectNode updated = objectMapper.createObjectNode();
        updated.put("totalMembers", 25);

        when(teamManagementService.updateSnapshot(any(User.class), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/team-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(25));

        verify(teamManagementService).updateSnapshot(any(User.class), any());
    }
}
