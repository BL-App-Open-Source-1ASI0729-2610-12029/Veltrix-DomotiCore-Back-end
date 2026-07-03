package com.domoticore.iam.application.commandservices;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.commands.ChangePasswordCommand;
import com.domoticore.iam.domain.model.commands.LoginUserCommand;
import com.domoticore.iam.domain.model.commands.RegisterUserCommand;
import com.domoticore.iam.domain.model.commands.UpdateUserCommand;
import com.domoticore.shared.application.result.Result;

public interface UserCommandService {

    Result<User, UserCommandFailure> register(RegisterUserCommand command);

    Result<User, UserCommandFailure> login(LoginUserCommand command);

    Result<User, UserCommandFailure> update(UpdateUserCommand command);

    Result<User, UserCommandFailure> changePassword(ChangePasswordCommand command);
}
