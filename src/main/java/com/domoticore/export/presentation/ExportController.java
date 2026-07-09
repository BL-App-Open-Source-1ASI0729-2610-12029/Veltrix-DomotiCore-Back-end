package com.domoticore.export.presentation;

import com.domoticore.export.application.ExportService;
import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.shared.security.PlatformPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/exports")
@Tag(name = "Data Export")
public class ExportController {

    private final ExportService exportService;
    private final CurrentUserProvider currentUserProvider;

    public ExportController(ExportService exportService, CurrentUserProvider currentUserProvider) {
        this.exportService = exportService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @Operation(summary = "Generate and download a data export in CSV, Excel-compatible CSV or PDF text format")
    public ResponseEntity<byte[]> export(@RequestBody Map<String, String> request) {
        currentUserProvider.requirePermission(PlatformPermission.EXPORT_DATA);
        var user = currentUserProvider.requireUser();
        String segment = currentUserProvider.requireSegment();

        ExportService.ExportResult result = exportService.export(
                user,
                segment,
                request.get("dataset"),
                request.get("format"),
                request.get("period"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(result.mediaType())
                .body(result.content());
    }
}
