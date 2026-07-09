package com.domoticore.export.presentation;

import com.domoticore.export.application.ExportService;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.JwtAuthenticationFilter;
import com.domoticore.shared.infrastructure.security.JwtService;
import com.domoticore.shared.infrastructure.security.PlatformPermission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void exportReturnsAttachment() throws Exception {
        when(currentUserProvider.requireUser()).thenReturn(User.newEmpty());
        when(currentUserProvider.requireSegment()).thenReturn("small-business");
        doNothing().when(currentUserProvider).requirePermission(PlatformPermission.EXPORT_DATA);
        when(exportService.export(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq("small-business"),
                        org.mockito.ArgumentMatchers.eq("energy-consumption"),
                        org.mockito.ArgumentMatchers.eq("csv"),
                        org.mockito.ArgumentMatchers.eq("week")))
                .thenReturn(new ExportService.ExportResult(
                        "domoticore-energy-consumption.csv",
                        MediaType.parseMediaType("text/csv"),
                        "period,total\nweek,12.4".getBytes()));

        mockMvc.perform(post("/api/v1/exports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataset\":\"energy-consumption\",\"format\":\"csv\",\"period\":\"week\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"domoticore-energy-consumption.csv\""));
    }
}
