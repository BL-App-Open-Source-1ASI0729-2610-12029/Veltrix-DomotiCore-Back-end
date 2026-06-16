package com.domoticore.history.presentation;

import com.domoticore.history.application.AlertsHistoryService;
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

@WebMvcTest(AlertsHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlertsHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertsHistoryService alertsHistoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getAlertsHistoryReturnsSnapshot() throws Exception {
        ObjectNode snapshot = new ObjectMapper().createObjectNode();
        snapshot.put("totalRecords", 12);
        snapshot.put("pageSize", 6);

        when(alertsHistoryService.getAlertsHistory()).thenReturn(snapshot);

        mockMvc.perform(get("/api/v1/alerts-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(12));
    }
}
