package com.domoticore.integrations.presentation;

import com.domoticore.integrations.application.IntegrationsService;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.JwtAuthenticationFilter;
import com.domoticore.shared.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntegrationsController.class)
@AutoConfigureMockMvc(addFilters = false)
class IntegrationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntegrationsService integrationsService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void compatibilityCheckReturnsSupportedDevice() throws Exception {
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("compatible", true);
        response.put("messageKey", "integrations.compatibility.supported");

        when(currentUserProvider.requireUserId()).thenReturn(2L);
        when(integrationsService.checkCompatibility(2L, "Veltrix Smart Bulb")).thenReturn(response);

        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("modelOrType", "Veltrix Smart Bulb");

        mockMvc.perform(post("/api/v1/integrations/compatibility-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compatible").value(true));
    }
}
