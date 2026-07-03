package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.iam.domain.model.aggregates.User;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DevicesOverviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class DevicesOverviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserCollectionAccessService userCollectionAccessService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private com.domoticore.shared.security.JwtService jwtService;

    @MockBean
    private com.domoticore.shared.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        User user = User.newEmpty();
        user.setId(1L);
        user.setRole("Admin");
        when(currentUserProvider.requireUser()).thenReturn(user);
        when(currentUserProvider.requireSegment()).thenReturn("smart-home");
    }

    @Test
    void listReturnsOverviewCollection() throws Exception {
        ObjectNode overview = objectMapper.createObjectNode();
        overview.put("id", 1);
        overview.put("totalDevices", 16);

        when(userCollectionAccessService.list(any(), any(), eq("devices-overview"))).thenReturn(List.of(overview));

        mockMvc.perform(get("/api/v1/devices-overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalDevices").value(16));
    }

    @Test
    void getByIdReturnsSingleOverview() throws Exception {
        ObjectNode overview = objectMapper.createObjectNode();
        overview.put("id", 1);
        overview.put("totalRooms", 4);

        when(userCollectionAccessService.getById(any(), any(), eq("devices-overview"), eq("1"))).thenReturn(overview);

        mockMvc.perform(get("/api/v1/devices-overview/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms").value(4));
    }

    @Test
    void patchUpdatesOverview() throws Exception {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("totalDevices", 17);

        ObjectNode updated = objectMapper.createObjectNode();
        updated.put("id", 1);
        updated.put("totalDevices", 17);

        when(userCollectionAccessService.patch(any(), any(), eq("devices-overview"), eq("1"), any()))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/devices-overview/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDevices").value(17));

        verify(userCollectionAccessService).patch(any(), any(), eq("devices-overview"), eq("1"), any());
    }
}
