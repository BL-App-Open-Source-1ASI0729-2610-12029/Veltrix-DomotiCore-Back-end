package com.domoticore.teammanagement.application;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class TeamZoneAccess {

    private static final Set<String> GLOBAL_ALIASES = Set.of(
            "global", "all", "mainoffice", "main-office", "hq", "headquarters");

    private TeamZoneAccess() {
    }

    public static boolean hasGlobalAccess(List<String> zones) {
        if (zones == null || zones.isEmpty()) {
            return true;
        }
        return zones.stream()
                .map(TeamZoneAccess::normalize)
                .anyMatch(GLOBAL_ALIASES::contains);
    }

    public static boolean canAccessZone(List<String> memberZones, String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return true;
        }
        if (memberZones == null || memberZones.isEmpty() || hasGlobalAccess(memberZones)) {
            return true;
        }

        String target = normalize(zoneId);
        for (String memberZone : memberZones) {
            String normalized = normalize(memberZone);
            if (normalized.equals(target)) {
                return true;
            }
            if (target.contains(normalized) || normalized.contains(target)) {
                return true;
            }
            if (matchesAlias(normalized, target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAlias(String memberZone, String targetZone) {
        return switch (memberZone) {
            case "mainoffice", "main-office", "hq", "headquarters" -> "office".equals(targetZone)
                    || "main-office".equals(targetZone)
                    || "retail".equals(targetZone);
            case "warehouse", "loading-dock", "loadingdock" -> "warehouse".equals(targetZone)
                    || targetZone.contains("warehouse")
                    || targetZone.contains("loading");
            case "office" -> "office".equals(targetZone) || "main-office".equals(targetZone);
            case "retail" -> "retail".equals(targetZone);
            case "living-room", "livingroom" -> "living-room".equals(targetZone);
            case "kitchen" -> "kitchen".equals(targetZone);
            case "master-bedroom", "masterbedroom", "bedroom" -> "master-bedroom".equals(targetZone)
                    || targetZone.contains("bedroom");
            default -> false;
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
