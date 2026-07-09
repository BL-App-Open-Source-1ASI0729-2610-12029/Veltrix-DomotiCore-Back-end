package com.domoticore.dashboard.presentation;

import com.domoticore.dashboard.application.DashboardService;
import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.shared.security.JwtAuthenticationFilter;
import com.domoticore.shared.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getDashboardReturnsAggregatePayload() throws Exception {
        ObjectNode dashboard = new ObjectMapper().createObjectNode();
        dashboard.put("totalDevices", 12);
        dashboard.put("hasDevices", true);

        when(currentUserProvider.requireUser()).thenReturn(new com.domoticore.iam.domain.model.aggregates.User());
        when(currentUserProvider.requireSegment()).thenReturn("smart-home");
        when(dashboardService.getDashboard(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("smart-home")))
                .thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDevices").value(12))
                .andExpect(jsonPath("$.hasDevices").value(true));
    }
}
