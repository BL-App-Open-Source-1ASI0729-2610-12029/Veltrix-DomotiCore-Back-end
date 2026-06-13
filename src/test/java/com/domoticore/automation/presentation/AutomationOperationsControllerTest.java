package com.domoticore.automation.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.security.JwtAuthenticationFilter;
import com.domoticore.shared.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutomationOperationsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AutomationOperationsControllerTest {

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
    void getRulesReturnsAutomationRules() throws Exception {
        var rule = objectMapper.createObjectNode();
        rule.put("id", "1");
        rule.put("name", "Dim if Office Empty");
        rule.put("active", false);

        when(jsonResourceService.findAll("automation-rules")).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/v1/automation/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dim if Office Empty"));
    }

    @Test
    void getShutdownProtocolReturnsSingleton() throws Exception {
        var protocol = objectMapper.createObjectNode();
        protocol.put("id", "closing-time");
        protocol.put("name", "Closing Time");
        protocol.put("triggersInMinutes", 15);
        protocol.set("steps", objectMapper.createArrayNode());

        when(jsonResourceService.findSingleton("automation-shutdown-protocol", "closing-time"))
                .thenReturn(protocol);

        mockMvc.perform(get("/api/v1/automation/shutdown-protocol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Closing Time"));
    }
}
