package com.domoticore.iam.interfaces.transform;

import com.domoticore.iam.application.commandservices.UserCommandFailure;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.interfaces.resources.AuthResponse;
import com.domoticore.iam.interfaces.resources.UserResponse;
import com.domoticore.shared.application.result.Result;
import com.domoticore.shared.exception.ConflictException;
import com.domoticore.shared.exception.ResourceNotFoundException;
import com.domoticore.shared.exception.UnauthorizedException;
import com.domoticore.shared.security.DomotiCoreUserDetails;
import com.domoticore.shared.security.JwtService;
import org.springframework.stereotype.Component;

@Component
public class UserResponseAssembler {

    private final JwtService jwtService;

    public UserResponseAssembler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(new DomotiCoreUserDetails(user));
        return new AuthResponse(token, UserResponse.from(user));
    }

    public AuthResponse toAuthResponse(Result<User, UserCommandFailure> result) {
        return result.fold(
                this::toAuthResponse,
                failure -> {
                    throw mapFailure(failure);
                }
        );
    }

    public UserResponse toUserResponse(Result<User, UserCommandFailure> result) {
        return result.fold(
                UserResponse::from,
                failure -> {
                    throw mapFailure(failure);
                }
        );
    }

    private RuntimeException mapFailure(UserCommandFailure failure) {
        if (failure instanceof UserCommandFailure.Duplicate) {
            return new ConflictException(failure.messageKey());
        }
        if (failure instanceof UserCommandFailure.InvalidCredentials) {
            return new UnauthorizedException(failure.messageKey());
        }
        return new ResourceNotFoundException(failure.messageKey());
    }
}
