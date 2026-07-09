package com.domoticore.export.application;

import com.domoticore.history.application.AlertsHistoryService;
import com.domoticore.history.application.EnergyIntelligenceService;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Service
public class ExportService {

    private static final Set<String> ALLOWED_DATASETS = Set.of(
            "energy-consumption", "alerts", "devices", "activity");
    private static final Set<String> ALLOWED_FORMATS = Set.of("csv", "excel", "pdf");

    private final EnergyIntelligenceService energyIntelligenceService;
    private final AlertsHistoryService alertsHistoryService;
    private final UserCollectionAccessService userCollectionAccessService;
    private final ObjectMapper objectMapper;

    public ExportService(
            EnergyIntelligenceService energyIntelligenceService,
            AlertsHistoryService alertsHistoryService,
            UserCollectionAccessService userCollectionAccessService,
            ObjectMapper objectMapper) {
        this.energyIntelligenceService = energyIntelligenceService;
        this.alertsHistoryService = alertsHistoryService;
        this.userCollectionAccessService = userCollectionAccessService;
        this.objectMapper = objectMapper;
    }

    public ExportResult export(User user, String segment, String dataset, String format, String period) {
        String resolvedDataset = normalize(dataset, ALLOWED_DATASETS, "energy-consumption");
        String resolvedFormat = normalize(format, ALLOWED_FORMATS, "csv");
        String resolvedPeriod = period == null || period.isBlank() ? "week" : period.trim();

        JsonNode payload = switch (resolvedDataset) {
            case "alerts" -> alertsHistoryService.getAlertsHistory(user, segment);
            case "devices" -> buildDevicesExport(user, segment);
            case "activity" -> buildActivityExport(user, segment);
            default -> energyIntelligenceService.getEnergyIntelligence(user, segment, resolvedPeriod);
        };

        if (isEmptyPayload(payload)) {
            throw new IllegalArgumentException("export.error.noData");
        }

        String filename = "domoticore-" + resolvedDataset + "." + extensionFor(resolvedFormat);
        byte[] content = buildContent(payload, resolvedDataset, resolvedFormat);
        return new ExportResult(filename, mediaTypeFor(resolvedFormat), content);
    }

    private JsonNode buildDevicesExport(User user, String segment) {
        List<JsonNode> devices = userCollectionAccessService.list(user, segment, "device-details");
        ArrayNode array = objectMapper.createArrayNode();
        devices.forEach(array::add);
        return array;
    }

    private JsonNode buildActivityExport(User user, String segment) {
        List<JsonNode> activity = userCollectionAccessService.list(user, segment, "activity-streams");
        ArrayNode array = objectMapper.createArrayNode();
        activity.forEach(array::add);
        return array;
    }

    private boolean isEmptyPayload(JsonNode payload) {
        if (payload == null || payload.isMissingNode()) {
            return true;
        }
        if (payload.isArray()) {
            return payload.isEmpty();
        }
        if (payload.isObject()) {
            return payload.isEmpty();
        }
        return false;
    }

    private byte[] buildContent(JsonNode payload, String dataset, String format) {
        if ("pdf".equals(format)) {
            return buildPdfReport(payload, dataset).getBytes(StandardCharsets.UTF_8);
        }
        return buildCsv(payload, dataset).getBytes(StandardCharsets.UTF_8);
    }

    private String buildCsv(JsonNode payload, String dataset) {
        StringBuilder csv = new StringBuilder();
        switch (dataset) {
            case "alerts" -> appendAlertsCsv(csv, payload);
            case "devices" -> appendDevicesCsv(csv, payload);
            case "activity" -> appendActivityCsv(csv, payload);
            default -> appendEnergyCsv(csv, payload);
        }
        return csv.toString();
    }

    private void appendEnergyCsv(StringBuilder csv, JsonNode payload) {
        csv.append("period,totalConsumptionKwh,trendPercent,trendDirection\n");
        csv.append(payload.path("period").asText()).append(',');
        csv.append(payload.path("totalConsumptionKwh").asDouble()).append(',');
        csv.append(payload.path("trendPercent").asDouble()).append(',');
        csv.append(payload.path("trendDirection").asText()).append('\n');
        csv.append("\ndevice,consumptionKwh,sharePercent\n");
        JsonNode devices = payload.path("devices");
        if (devices.isArray()) {
            for (JsonNode device : devices) {
                csv.append(device.path("name").asText()).append(',');
                csv.append(device.path("consumptionKwh").asDouble()).append(',');
                csv.append(device.path("sharePercent").asDouble()).append('\n');
            }
        }
    }

    private void appendAlertsCsv(StringBuilder csv, JsonNode payload) {
        csv.append("title,location,priority,status,category,timestamp\n");
        JsonNode log = payload.path("log");
        if (!log.isArray()) {
            return;
        }
        for (JsonNode entry : log) {
            csv.append(entry.path("title").asText()).append(',');
            csv.append(entry.path("location").asText()).append(',');
            csv.append(entry.path("priority").asText()).append(',');
            csv.append(entry.path("status").asText()).append(',');
            csv.append(entry.path("category").asText()).append(',');
            csv.append(entry.path("timestamp").asText()).append('\n');
        }
    }

    private void appendDevicesCsv(StringBuilder csv, JsonNode payload) {
        csv.append("id,name,type,active,connection,batteryPercent,powerUsageW\n");
        if (!payload.isArray()) {
            return;
        }
        for (JsonNode device : payload) {
            csv.append(device.path("id").asText()).append(',');
            csv.append(device.path("name").asText(device.path("deviceName").asText())).append(',');
            csv.append(device.path("type").asText(device.path("category").asText())).append(',');
            csv.append(device.path("active").asBoolean(device.path("powerOn").asBoolean(false))).append(',');
            csv.append(device.path("connection").asText(device.path("status").asText())).append(',');
            csv.append(device.path("batteryPercent").asText("")).append(',');
            csv.append(device.path("powerUsageW").asText("")).append('\n');
        }
    }

    private void appendActivityCsv(StringBuilder csv, JsonNode payload) {
        csv.append("id,title,description,timestamp,type\n");
        if (!payload.isArray()) {
            return;
        }
        for (JsonNode entry : payload) {
            csv.append(entry.path("id").asText()).append(',');
            csv.append(entry.path("title").asText(entry.path("titleKey").asText())).append(',');
            csv.append(entry.path("description").asText(entry.path("descriptionKey").asText())).append(',');
            csv.append(entry.path("timestamp").asText(entry.path("timeLabel").asText())).append(',');
            csv.append(entry.path("type").asText(entry.path("eventType").asText())).append('\n');
        }
    }

    private String buildPdfReport(JsonNode payload, String dataset) {
        StringBuilder report = new StringBuilder();
        report.append("DomotiCore Export Report\n");
        report.append("Dataset: ").append(dataset).append('\n');
        report.append("Generated by DomotiCore backend export service\n\n");
        report.append(payload.toPrettyString());
        return report.toString();
    }

    private String normalize(String value, Set<String> allowed, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase();
        return allowed.contains(normalized) ? normalized : fallback;
    }

    private String extensionFor(String format) {
        return switch (format) {
            case "excel" -> "csv";
            case "pdf" -> "txt";
            default -> "csv";
        };
    }

    private MediaType mediaTypeFor(String format) {
        return switch (format) {
            case "pdf" -> MediaType.TEXT_PLAIN;
            case "excel" -> new MediaType("text", "csv");
            default -> MediaType.parseMediaType("text/csv");
        };
    }

    public record ExportResult(String filename, MediaType mediaType, byte[] content) {}
}
