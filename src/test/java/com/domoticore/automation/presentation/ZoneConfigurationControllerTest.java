package com.domoticore.automation.presentation;

import com.domoticore.automation.application.ZoneConfigurationService;
import com.domoticore.shared.security.JwtAuthenticationFilter;
import com.domoticore.shared.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ZoneConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ZoneConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ZoneConfigurationService zoneConfigurationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getZoneConfigurationReturnsPayload() throws Exception {
        ObjectNode configuration = objectMapper.createObjectNode();
        configuration.put("primaryZoneId", "main-office");
        configuration.put("globalOptimizerScore", 94);

        when(zoneConfigurationService.getConfiguration()).thenReturn(configuration);

        mockMvc.perform(get("/api/v1/zone-configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryZoneId").value("main-office"));
    }

    @Test
    void patchZoneConfigurationUpdatesPayload() throws Exception {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("globalOptimizerScore", 96);

        ObjectNode updated = objectMapper.createObjectNode();
        updated.put("primaryZoneId", "main-office");
        updated.put("globalOptimizerScore", 96);

        when(zoneConfigurationService.updateConfiguration(any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/zone-configuration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.globalOptimizerScore").value(96));

        verify(zoneConfigurationService).updateConfiguration(any());
    }
}
