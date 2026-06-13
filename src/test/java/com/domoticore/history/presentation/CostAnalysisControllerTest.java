package com.domoticore.history.presentation;

import com.domoticore.history.application.CostAnalysisService;
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

@WebMvcTest(CostAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
class CostAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CostAnalysisService costAnalysisService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getCostAnalysisReturnsPayload() throws Exception {
        ObjectNode analysis = new ObjectMapper().createObjectNode();
        analysis.put("totalBilling", 12482.5);
        analysis.put("billingCycleLabel", "Oct 01 - Oct 31");

        when(costAnalysisService.getCostAnalysis()).thenReturn(analysis);

        mockMvc.perform(get("/api/v1/cost-analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBilling").value(12482.5));
    }
}
