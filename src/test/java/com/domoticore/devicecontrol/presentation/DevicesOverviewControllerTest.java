package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.security.JwtAuthenticationFilter;
import com.domoticore.shared.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
    private JsonResourceService jsonResourceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listReturnsOverviewCollection() throws Exception {
        ObjectNode overview = objectMapper.createObjectNode();
        overview.put("id", 1);
        overview.put("totalDevices", 16);

        when(jsonResourceService.findAll("devices-overview")).thenReturn(List.of(overview));

        mockMvc.perform(get("/api/v1/devices-overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalDevices").value(16));
    }

    @Test
    void getByIdReturnsSingleOverview() throws Exception {
        ObjectNode overview = objectMapper.createObjectNode();
        overview.put("id", 1);
        overview.put("totalRooms", 4);

        when(jsonResourceService.findById("devices-overview", "1")).thenReturn(overview);

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

        when(jsonResourceService.patch(eq("devices-overview"), eq("1"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/devices-overview/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDevices").value(17));

        verify(jsonResourceService).patch(eq("devices-overview"), eq("1"), org.mockito.ArgumentMatchers.any());
    }
}
