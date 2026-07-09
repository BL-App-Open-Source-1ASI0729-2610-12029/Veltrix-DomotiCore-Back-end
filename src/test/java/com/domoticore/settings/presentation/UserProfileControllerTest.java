package com.domoticore.settings.presentation;

import com.domoticore.settings.application.UserProfileService;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.JwtAuthenticationFilter;
import com.domoticore.shared.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getMyProfileReturnsAuthenticatedUserPayload() throws Exception {
        when(currentUserProvider.requireUserId()).thenReturn(2L);

        ObjectNode profile = objectMapper.createObjectNode();
        profile.put("fullName", "User Two");
        profile.put("email", "user2@domoticore.local");

        when(userProfileService.getProfile(2L)).thenReturn(profile);

        mockMvc.perform(get("/api/v1/user-profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("User Two"));

        verify(userProfileService).getProfile(2L);
    }

    @Test
    void patchMyProfileUpdatesAuthenticatedUserPayload() throws Exception {
        when(currentUserProvider.requireUserId()).thenReturn(2L);

        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("fullName", "Updated Name");

        ObjectNode updated = objectMapper.createObjectNode();
        updated.put("fullName", "Updated Name");
        updated.put("email", "user2@domoticore.local");

        when(userProfileService.updateProfile(eq(2L), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/user-profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"));

        verify(userProfileService).updateProfile(eq(2L), any());
    }
}
