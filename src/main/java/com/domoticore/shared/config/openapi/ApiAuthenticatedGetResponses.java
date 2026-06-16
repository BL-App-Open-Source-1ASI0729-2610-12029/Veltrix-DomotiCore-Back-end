package com.domoticore.shared.config.openapi;

import com.domoticore.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK - Datos listos"),
        @ApiResponse(responseCode = "401", description = "No autenticado",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class)))
})
public @interface ApiAuthenticatedGetResponses {
}
