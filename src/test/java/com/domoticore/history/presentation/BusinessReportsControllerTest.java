package com.domoticore.history.presentation;

import com.domoticore.history.application.BusinessReportsService;
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

@WebMvcTest(BusinessReportsController.class)
@AutoConfigureMockMvc(addFilters = false)
class BusinessReportsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessReportsService businessReportsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getBusinessReportsReturnsMonthlySnapshot() throws Exception {
        ObjectNode snapshot = new ObjectMapper().createObjectNode();
        snapshot.put("id", "thisMonth");
        snapshot.put("efficiencyGoalPercent", 75);

        when(businessReportsService.getBusinessReports("thisMonth")).thenReturn(snapshot);

        mockMvc.perform(get("/api/v1/business-reports").param("range", "thisMonth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.efficiencyGoalPercent").value(75));
    }
}
