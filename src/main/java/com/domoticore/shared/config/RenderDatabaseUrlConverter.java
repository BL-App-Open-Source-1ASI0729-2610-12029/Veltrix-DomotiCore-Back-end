package com.domoticore.shared.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

final class RenderDatabaseUrlConverter {

    private RenderDatabaseUrlConverter() {
    }

    record JdbcConnectionDetails(String jdbcUrl, String username, String password) {
    }

    static JdbcConnectionDetails resolve(
            String databaseUrl,
            String host,
            String port,
            String databaseName,
            String username,
            String password) {
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            return fromDatabaseUrl(databaseUrl.trim(), username, password);
        }

        if (host != null && !host.isBlank()) {
            String jdbcUrl = "jdbc:postgresql://"
                    + host.trim()
                    + ":"
                    + defaultPort(port)
                    + "/"
                    + defaultDatabase(databaseName)
                    + sslQuery(host);
            return new JdbcConnectionDetails(jdbcUrl, username, password);
        }

        throw new IllegalStateException(
                "Database is not configured. Link the PostgreSQL instance on Render or set DATABASE_URL.");
    }

    private static JdbcConnectionDetails fromDatabaseUrl(
            String databaseUrl,
            String fallbackUsername,
            String fallbackPassword) {
        if (databaseUrl.startsWith("jdbc:")) {
            return new JdbcConnectionDetails(
                    ensureSslQuery(databaseUrl),
                    fallbackUsername,
                    fallbackPassword);
        }

        String normalized = databaseUrl.replaceFirst("^postgres://", "postgresql://");
        if (!normalized.startsWith("postgresql://")) {
            throw new IllegalArgumentException("Unsupported DATABASE_URL format: " + databaseUrl);
        }

        String withoutScheme = normalized.substring("postgresql://".length());
        int at = withoutScheme.lastIndexOf('@');
        String userInfo = null;
        String hostAndPath;

        if (at >= 0) {
            userInfo = withoutScheme.substring(0, at);
            hostAndPath = withoutScheme.substring(at + 1);
        } else {
            hostAndPath = withoutScheme;
        }

        String resolvedUsername = fallbackUsername;
        String resolvedPassword = fallbackPassword;
        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            resolvedUsername = decode(parts[0]);
            if (parts.length > 1) {
                resolvedPassword = decode(parts[1]);
            }
        }

        int slash = hostAndPath.indexOf('/');
        String hostPort = slash >= 0 ? hostAndPath.substring(0, slash) : hostAndPath;
        String database = slash >= 0 && hostAndPath.length() > slash + 1
                ? hostAndPath.substring(slash + 1)
                : defaultDatabase(null);

        int queryIndex = database.indexOf('?');
        String query = null;
        if (queryIndex >= 0) {
            query = database.substring(queryIndex + 1);
            database = database.substring(0, queryIndex);
        }

        int colon = hostPort.lastIndexOf(':');
        String host = colon >= 0 ? hostPort.substring(0, colon) : hostPort;
        String port = colon >= 0 ? hostPort.substring(colon + 1) : defaultPort(null);

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + sslQuery(host);
        if (query != null && !query.isBlank()) {
            jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + query;
        }

        return new JdbcConnectionDetails(jdbcUrl, resolvedUsername, resolvedPassword);
    }

    private static String defaultPort(String port) {
        return port == null || port.isBlank() ? "5432" : port.trim();
    }

    private static String defaultDatabase(String databaseName) {
        return databaseName == null || databaseName.isBlank() ? "domoticore" : databaseName.trim();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String sslQuery(String host) {
        if (host != null && host.contains("render.com")) {
            return "?sslmode=require";
        }
        return "";
    }

    private static String ensureSslQuery(String jdbcUrl) {
        if (jdbcUrl.contains("render.com") && !jdbcUrl.contains("sslmode=")) {
            return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
        }
        return jdbcUrl;
    }
}
