package com.domoticore.iam.infrastructure;

import com.domoticore.iam.domain.model.commands.LoginUserCommand;
import com.domoticore.iam.domain.model.commands.RegisterUserCommand;
import com.domoticore.iam.domain.model.commands.UpdateUserCommand;
import com.domoticore.iam.domain.model.valueobjects.Email;
import com.domoticore.iam.infrastructure.LoginRequest;
import com.domoticore.iam.infrastructure.RegisterRequest;
import com.domoticore.iam.infrastructure.UpdateUserRequest;
import org.springframework.stereotype.Component;

@Component
public class UserCommandFromResourceAssembler {

    public RegisterUserCommand toRegisterCommand(RegisterRequest resource) {
        return new RegisterUserCommand(resource.name(), new Email(resource.email()), resource.password());
    }

    public LoginUserCommand toLoginCommand(LoginRequest resource) {
        return new LoginUserCommand(new Email(resource.email()), resource.password());
    }

    public UpdateUserCommand toUpdateCommand(Long userId, UpdateUserRequest resource) {
        return new UpdateUserCommand(
                userId,
                resource.name(),
                resource.email(),
                resource.role(),
                resource.avatar(),
                resource.accountType(),
                resource.onboardingCompleted()
        );
    }
}
