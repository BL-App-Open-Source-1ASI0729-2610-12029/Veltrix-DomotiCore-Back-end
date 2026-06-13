package com.domoticore.iam.presentation;

import com.domoticore.iam.application.AuthService;
import com.domoticore.iam.presentation.dto.UpdateUserRequest;
import com.domoticore.iam.presentation.dto.UserResponse;
import com.domoticore.shared.exception.ForbiddenException;
import com.domoticore.shared.security.CurrentUserProvider;
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
    private final CurrentUserProvider currentUserProvider;

    public UserController(AuthService authService, CurrentUserProvider currentUserProvider) {
        this.authService = authService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @Operation(summary = "List auth users (without password)")
    public List<UserResponse> listUsers() {
        return authService.listUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get auth user profile")
    public UserResponse getUser(@PathVariable Long id) {
        assertSelf(id);
        return authService.getUser(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user profile / onboarding")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        assertSelf(id);
        return authService.updateUser(id, request);
    }

    private void assertSelf(Long id) {
        if (!id.equals(currentUserProvider.requireUserId())) {
            throw new ForbiddenException("You can only access your own user profile");
        }
    }
}
