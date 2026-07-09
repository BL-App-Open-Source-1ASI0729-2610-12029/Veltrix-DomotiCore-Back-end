package com.domoticore.iam.infrastructure;

import com.domoticore.iam.application.commandservices.UserCommandFailure;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.infrastructure.AuthResponse;
import com.domoticore.iam.infrastructure.UserResponse;
import com.domoticore.shared.domain.model.Result;
import com.domoticore.shared.domain.model.ConflictException;
import com.domoticore.shared.domain.model.ResourceNotFoundException;
import com.domoticore.shared.domain.model.UnauthorizedException;
import com.domoticore.shared.infrastructure.security.DomotiCoreUserDetails;
import com.domoticore.shared.infrastructure.security.JwtService;
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
        if (failure instanceof UserCommandFailure.InvalidCredentials
                || failure instanceof UserCommandFailure.WrongPassword) {
            return new UnauthorizedException(failure.messageKey());
        }
        return new ResourceNotFoundException(failure.messageKey());
    }
}
