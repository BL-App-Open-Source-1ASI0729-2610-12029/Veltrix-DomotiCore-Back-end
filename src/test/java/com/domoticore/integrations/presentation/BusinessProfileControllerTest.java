package com.domoticore.integrations.presentation;

import com.domoticore.integrations.application.BusinessProfileService;
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

@WebMvcTest(BusinessProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class BusinessProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessProfileService businessProfileService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getBusinessProfileReturnsPayload() throws Exception {
        ObjectNode profile = objectMapper.createObjectNode();
        profile.put("businessName", "Sterling Energy Solutions LLC");
        profile.put("tin", "XX-XXXX5678");

        when(businessProfileService.getProfile()).thenReturn(profile);

        mockMvc.perform(get("/api/v1/business-profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Sterling Energy Solutions LLC"));
    }

    @Test
    void patchBusinessProfileUpdatesPayload() throws Exception {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("businessName", "Updated LLC");

        ObjectNode updated = objectMapper.createObjectNode();
        updated.put("businessName", "Updated LLC");
        updated.put("tin", "XX-XXXX5678");

        when(businessProfileService.updateProfile(any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/business-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Updated LLC"));

        verify(businessProfileService).updateProfile(any());
    }
}
