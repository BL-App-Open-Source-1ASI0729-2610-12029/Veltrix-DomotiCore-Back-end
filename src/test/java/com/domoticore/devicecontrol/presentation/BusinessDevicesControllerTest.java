package com.domoticore.devicecontrol.presentation;

import com.domoticore.devicecontrol.application.BusinessDevicesService;
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

@WebMvcTest(BusinessDevicesController.class)
@AutoConfigureMockMvc(addFilters = false)
class BusinessDevicesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessDevicesService businessDevicesService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        User user = User.newEmpty();
        user.setId(7L);
        when(currentUserProvider.requireUser()).thenReturn(user);
        when(currentUserProvider.requireSegment()).thenReturn("small-business");
    }

    @Test
    void getOverviewReturnsBusinessDevicesSnapshot() throws Exception {
        ObjectNode overview = new ObjectMapper().createObjectNode();
        overview.put("activeDeviceCount", 42);
        overview.put("totalConsumptionKw", 14.2);

        when(businessDevicesService.getOverview(any(), eq("small-business"))).thenReturn(overview);

        mockMvc.perform(get("/api/v1/business-devices/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeDeviceCount").value(42));
    }
}
