package com.domoticore.devicecontrol.presentation;

import com.domoticore.devicecontrol.application.DeviceExplorerService;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.JwtAuthenticationFilter;
import com.domoticore.shared.infrastructure.security.JwtService;
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

@WebMvcTest(DeviceExplorerController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeviceExplorerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceExplorerService deviceExplorerService;

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
    void getDeviceExplorerReturnsSnapshot() throws Exception {
        ObjectNode snapshot = new ObjectMapper().createObjectNode();
        snapshot.put("totalResults", 34);
        snapshot.put("liveCoveragePercent", 98.2);

        when(deviceExplorerService.getDeviceExplorer(any(), eq("small-business"))).thenReturn(snapshot);

        mockMvc.perform(get("/api/v1/device-explorer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(34));
    }
}
