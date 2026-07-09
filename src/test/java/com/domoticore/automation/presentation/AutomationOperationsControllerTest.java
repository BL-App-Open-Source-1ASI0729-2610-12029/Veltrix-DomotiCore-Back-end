package com.domoticore.automation.presentation;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.automation.application.AutomationActionService;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.JwtAuthenticationFilter;
import com.domoticore.shared.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private UserCollectionAccessService userCollectionAccessService;

    @MockBean
    private AutomationActionService automationActionService;

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
        when(currentUserProvider.requireSegment()).thenReturn("small-business");
    }

    @Test
    void getRulesReturnsAutomationRules() throws Exception {
        var rule = objectMapper.createObjectNode();
        rule.put("id", "1");
        rule.put("name", "Dim if Office Empty");
        rule.put("active", false);

        when(userCollectionAccessService.list(any(), any(), eq("automation-rules"))).thenReturn(List.of(rule));

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

        when(userCollectionAccessService.getSingleton(
                any(), any(), eq("automation-shutdown-protocol"), eq("closing-time")))
                .thenReturn(protocol);

        mockMvc.perform(get("/api/v1/automation/shutdown-protocol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Closing Time"));
    }
}
