package com.domoticore.smeoperations.presentation;

import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.shared.security.JwtAuthenticationFilter;
import com.domoticore.shared.security.JwtService;
import com.domoticore.smeoperations.application.OperationsHubService;
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

@WebMvcTest(OperationsHubController.class)
@AutoConfigureMockMvc(addFilters = false)
class OperationsHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperationsHubService operationsHubService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getSnapshotReturnsAuthenticatedUserRangePayload() throws Exception {
        ObjectNode snapshot = new ObjectMapper().createObjectNode();
        snapshot.put("criticalAlertCount", 3);

        when(currentUserProvider.requireUserId()).thenReturn(7L);
        when(operationsHubService.getSnapshot(7L, "thisMonth")).thenReturn(snapshot);

        mockMvc.perform(get("/api/v1/operations-hub/snapshot?range=thisMonth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalAlertCount").value(3));
    }
}
