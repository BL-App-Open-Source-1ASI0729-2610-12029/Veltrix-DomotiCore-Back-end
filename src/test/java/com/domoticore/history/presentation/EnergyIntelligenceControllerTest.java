package com.domoticore.history.presentation;

import com.domoticore.history.application.EnergyIntelligenceService;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.shared.security.JwtAuthenticationFilter;
import com.domoticore.shared.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnergyIntelligenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnergyIntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnergyIntelligenceService energyIntelligenceService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        User user = User.newEmpty();
        user.setId(1L);
        when(currentUserProvider.requireUser()).thenReturn(user);
        when(currentUserProvider.requireSegment()).thenReturn("smart-home");
    }

    @Test
    void getEnergyIntelligenceReturnsWeeklySnapshot() throws Exception {
        ObjectNode snapshot = new ObjectMapper().createObjectNode();
        snapshot.put("id", "week");
        snapshot.put("period", "week");
        snapshot.put("totalConsumptionKwh", 42.8);

        when(energyIntelligenceService.getEnergyIntelligence(any(), eq("smart-home"), eq("week")))
                .thenReturn(snapshot);

        mockMvc.perform(get("/api/v1/energy-intelligence").param("period", "week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConsumptionKwh").value(42.8));
    }
}
