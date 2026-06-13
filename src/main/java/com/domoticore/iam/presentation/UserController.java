package com.domoticore.iam.presentation;

import com.domoticore.iam.application.AuthService;
import com.domoticore.iam.presentation.dto.UpdateUserRequest;
import com.domoticore.iam.presentation.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "List auth users (without password)")
    public List<UserResponse> listUsers() {
        return authService.listUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get auth user profile")
    public UserResponse getUser(@PathVariable Long id) {
        return authService.getUser(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user profile / onboarding")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return authService.updateUser(id, request);
    }
}
