package com.domoticore.shared.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderDatabaseUrlConverterTest {

    @Test
    void prefersDatabaseUrlOverExternalUrl() {
        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                "postgresql://domoticore_v75r_user:secret@dpg-test-a/domoticore_v75r",
                "postgresql://wrong:wrong@dpg-other-a/other",
                null,
                null,
                null,
                null,
                null);

        assertEquals("jdbc:postgresql://dpg-test-a:5432/domoticore_v75r", details.jdbcUrl());
        assertEquals("domoticore_v75r_user", details.username());
        assertEquals("secret", details.password());
    }

    @Test
    void convertsRenderExternalPostgresUrlToJdbc() {
        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                "postgresql://domoticore:secret@dpg-test-a.oregon-postgres.render.com:5432/domoticore",
                null,
                null,
                null,
                null,
                null,
                null);

        assertEquals(
                "jdbc:postgresql://dpg-test-a.oregon-postgres.render.com:5432/domoticore?sslmode=require",
                details.jdbcUrl());
        assertEquals("domoticore", details.username());
        assertEquals("secret", details.password());
    }

    @Test
    void convertsShortRenderHostFromDbParts() {
        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                null,
                null,
                "dpg-test-a",
                "5432",
                "domoticore",
                "domoticore",
                "secret");

        assertEquals("jdbc:postgresql://dpg-test-a:5432/domoticore", details.jdbcUrl());
        assertEquals("domoticore", details.username());
        assertEquals("secret", details.password());
    }

    @Test
    void stripsSslFromInternalJdbcUrl() {
        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                null,
                "jdbc:postgresql://dpg-test-a:5432/domoticore_v75r?sslmode=require",
                null,
                null,
                null,
                "domoticore_v75r_user",
                "secret");

        assertEquals("jdbc:postgresql://dpg-test-a:5432/domoticore_v75r", details.jdbcUrl());
        assertFalse(details.jdbcUrl().contains("sslmode"));
    }

    @Test
    void keepsJdbcUrlWithSslForRenderExternalHost() {
        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                null,
                "jdbc:postgresql://dpg-test-a.oregon-postgres.render.com:5432/domoticore",
                null,
                null,
                null,
                "domoticore",
                "secret");

        assertTrue(details.jdbcUrl().contains("sslmode=require"));
    }
}
