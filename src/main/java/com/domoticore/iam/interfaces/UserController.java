package com.domoticore.iam.interfaces;

import com.domoticore.iam.application.commandservices.UserCommandService;
import com.domoticore.iam.application.queryservices.UserQueryService;
import com.domoticore.iam.domain.model.queries.GetUserByIdQuery;
import com.domoticore.iam.interfaces.resources.ChangePasswordRequest;
import com.domoticore.iam.interfaces.resources.UpdateUserRequest;
import com.domoticore.iam.interfaces.resources.UserResponse;
import com.domoticore.iam.interfaces.transform.UserCommandFromResourceAssembler;
import com.domoticore.iam.interfaces.transform.UserResponseAssembler;
import com.domoticore.shared.config.openapi.ApiUserSelfResponses;
import com.domoticore.shared.exception.ForbiddenException;
import com.domoticore.shared.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserCommandFromResourceAssembler commandAssembler;
    private final UserResponseAssembler responseAssembler;
    private final CurrentUserProvider currentUserProvider;

    public UserController(
            UserCommandService userCommandService,
            UserQueryService userQueryService,
            UserCommandFromResourceAssembler commandAssembler,
            UserResponseAssembler responseAssembler,
            CurrentUserProvider currentUserProvider) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.commandAssembler = commandAssembler;
        this.responseAssembler = responseAssembler;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/{id}")
    @ApiUserSelfResponses
    @Operation(summary = "Get auth user profile")
    public UserResponse getUser(@PathVariable Long id) {
        assertSelf(id);
        return userQueryService.handle(new GetUserByIdQuery(id))
                .map(UserResponse::from)
                .orElseThrow(() -> new com.domoticore.shared.exception.ResourceNotFoundException(
                        "iam.user.error.notFound: " + id));
    }

    @PatchMapping("/{id}")
    @ApiUserSelfResponses
    @Operation(summary = "Update user profile / onboarding")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        assertSelf(id);
        return responseAssembler.toUserResponse(
                userCommandService.update(commandAssembler.toUpdateCommand(id, request))
        );
    }

    @PostMapping("/{id}/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiUserSelfResponses
    @Operation(summary = "Change authenticated user password")
    public void changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        assertSelf(id);
        responseAssembler.toUserResponse(
                userCommandService.changePassword(
                        new com.domoticore.iam.domain.model.commands.ChangePasswordCommand(
                                id,
                                request.currentPassword(),
                                request.newPassword()))
        );
    }

    private void assertSelf(Long id) {
        if (!id.equals(currentUserProvider.requireUserId())) {
            throw new ForbiddenException("iam.user.error.forbidden");
        }
    }
}
