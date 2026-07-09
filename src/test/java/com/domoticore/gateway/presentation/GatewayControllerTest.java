package com.domoticore.gateway.presentation;

import com.domoticore.gateway.application.GatewayService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GatewayController.class)
@AutoConfigureMockMvc(addFilters = false)
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GatewayService gatewayService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void linkGatewayReturnsLinkedDevice() throws Exception {
        ObjectNode gateway = objectMapper.createObjectNode();
        gateway.put("id", "gw-primary");
        gateway.put("macOrId", "VELTRIX-GW-DEMO");
        gateway.put("status", "online");

        when(currentUserProvider.requireUserId()).thenReturn(3L);
        when(gatewayService.linkGateway(eq(3L), eq("VELTRIX-GW-DEMO"), eq("Home GW"))).thenReturn(gateway);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("macOrId", "VELTRIX-GW-DEMO");
        body.put("label", "Home GW");

        mockMvc.perform(post("/api/v1/gateways/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macOrId").value("VELTRIX-GW-DEMO"));
    }

    @Test
    void getCurrentGatewayReturnsNullWhenUnlinked() throws Exception {
        when(currentUserProvider.requireUserId()).thenReturn(3L);
        when(gatewayService.getGateway(anyLong())).thenReturn(objectMapper.nullNode());

        mockMvc.perform(get("/api/v1/gateways/current"))
                .andExpect(status().isOk());
    }
}
