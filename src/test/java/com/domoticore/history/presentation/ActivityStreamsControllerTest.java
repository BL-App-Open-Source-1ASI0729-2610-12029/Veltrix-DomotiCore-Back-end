package com.domoticore.history.presentation;

import com.domoticore.history.application.ActivityStreamService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityStreamsController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActivityStreamsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityStreamService activityStreamService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        when(currentUserProvider.requireUser()).thenReturn(User.newEmpty());
        when(currentUserProvider.requireSegment()).thenReturn("smart-home");
    }

    @Test
    void listReturnsRoleFilteredEntries() throws Exception {
        ObjectNode entry = objectMapper.createObjectNode();
        entry.put("id", "as-1");
        entry.put("userId", 2);

        when(activityStreamService.list(any(User.class), eq("smart-home"))).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/activity-streams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("as-1"))
                .andExpect(jsonPath("$[0].userId").value(2));
    }

    @Test
    void createStampsActorServerSide() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("id", "as-new");
        body.put("deviceName", "Test Device");

        ObjectNode created = body.deepCopy();
        created.put("userId", 7);

        when(activityStreamService.create(any(User.class), eq("smart-home"), any())).thenReturn(created);

        mockMvc.perform(post("/api/v1/activity-streams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(7));

        verify(activityStreamService).create(any(User.class), eq("smart-home"), any());
    }
}
