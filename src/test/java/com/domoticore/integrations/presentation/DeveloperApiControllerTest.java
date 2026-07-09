package com.domoticore.integrations.presentation;

import com.domoticore.integrations.application.DeveloperApiService;
import com.domoticore.shared.security.CurrentUserProvider;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeveloperApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeveloperApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeveloperApiService developerApiService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listDevicesReturnsOk() throws Exception {
        ArrayNode devices = new ObjectMapper().createArrayNode();
        when(currentUserProvider.requireUser()).thenReturn(new com.domoticore.iam.domain.model.aggregates.User());
        when(developerApiService.listDeviceStatuses(org.mockito.ArgumentMatchers.any())).thenReturn(devices);

        mockMvc.perform(get("/api/v1/developer/devices"))
                .andExpect(status().isOk());
    }
}
