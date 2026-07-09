package com.domoticore.iam.presentation;

import com.domoticore.iam.application.commandservices.UserCommandService;
import com.domoticore.iam.infrastructure.AuthResponse;
import com.domoticore.iam.infrastructure.LoginRequest;
import com.domoticore.iam.infrastructure.RegisterRequest;
import com.domoticore.iam.infrastructure.UserCommandFromResourceAssembler;
import com.domoticore.iam.infrastructure.UserResponseAssembler;
import com.domoticore.shared.infrastructure.config.openapi.ApiLoginResponses;
import com.domoticore.shared.infrastructure.config.openapi.ApiRegisterResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "IAM")
public class AuthController {

    private final UserCommandService userCommandService;
    private final UserCommandFromResourceAssembler commandAssembler;
    private final UserResponseAssembler responseAssembler;

    public AuthController(
            UserCommandService userCommandService,
            UserCommandFromResourceAssembler commandAssembler,
            UserResponseAssembler responseAssembler) {
        this.userCommandService = userCommandService;
        this.commandAssembler = commandAssembler;
        this.responseAssembler = responseAssembler;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiRegisterResponses
    @Operation(summary = "Register a new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return responseAssembler.toAuthResponse(
                userCommandService.register(commandAssembler.toRegisterCommand(request))
        );
    }

    @PostMapping("/auth/login")
    @ResponseStatus(HttpStatus.OK)
    @ApiLoginResponses
    @Operation(summary = "Login and receive JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return responseAssembler.toAuthResponse(
                userCommandService.login(commandAssembler.toLoginCommand(request))
        );
    }
}
