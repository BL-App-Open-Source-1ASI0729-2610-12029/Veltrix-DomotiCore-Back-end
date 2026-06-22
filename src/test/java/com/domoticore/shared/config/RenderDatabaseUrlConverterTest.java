package com.domoticore.shared.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderDatabaseUrlConverterTest {

    @Test
    void convertsRenderPostgresUrlToJdbc() {
        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                "postgresql://domoticore:secret@dpg-test-a.oregon-postgres.render.com:5432/domoticore",
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
    void keepsJdbcUrlWithSslForRender() {
        RenderDatabaseUrlConverter.JdbcConnectionDetails details = RenderDatabaseUrlConverter.resolve(
                "jdbc:postgresql://dpg-test-a.oregon-postgres.render.com:5432/domoticore",
                null,
                null,
                null,
                "domoticore",
                "secret");

        assertTrue(details.jdbcUrl().contains("sslmode=require"));
    }
}
