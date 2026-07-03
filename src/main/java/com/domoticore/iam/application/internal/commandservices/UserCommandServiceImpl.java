package com.domoticore.iam.application.internal.commandservices;

import com.domoticore.iam.application.commandservices.UserCommandFailure;
import com.domoticore.iam.application.commandservices.UserCommandService;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.commands.ChangePasswordCommand;
import com.domoticore.iam.domain.model.commands.LoginUserCommand;
import com.domoticore.iam.domain.model.commands.RegisterUserCommand;
import com.domoticore.iam.domain.model.commands.UpdateUserCommand;
import com.domoticore.iam.infrastructure.persistence.jpa.UserRepository;
import com.domoticore.settings.application.UserProfileService;
import com.domoticore.shared.application.result.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserProfileService userProfileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userProfileService = userProfileService;
    }

    @Override
    @Transactional
    public Result<User, UserCommandFailure> register(RegisterUserCommand command) {
        if (userRepository.existsByEmailAddress(command.email())) {
            return Result.failure(new UserCommandFailure.Duplicate());
        }

        User user = new User(command, passwordEncoder.encode(command.rawPassword()));
        User saved = userRepository.save(user);
        userProfileService.ensureProfile(saved.getId());
        return Result.success(saved);
    }

    @Override
    public Result<User, UserCommandFailure> login(LoginUserCommand command) {
        User user = userRepository.findByEmailAddress(command.email()).orElse(null);
        if (user == null || !passwordEncoder.matches(command.rawPassword(), user.getPasswordHash())) {
            return Result.failure(new UserCommandFailure.InvalidCredentials());
        }
        return Result.success(user);
    }

    @Override
    @Transactional
    public Result<User, UserCommandFailure> update(UpdateUserCommand command) {
        return userRepository.findById(command.userId())
                .map(user -> {
                    user.apply(command);
                    return Result.<User, UserCommandFailure>success(userRepository.save(user));
                })
                .orElse(Result.failure(new UserCommandFailure.NotFound()));
    }

    @Override
    @Transactional
    public Result<User, UserCommandFailure> changePassword(ChangePasswordCommand command) {
        return userRepository.findById(command.userId())
                .map(user -> {
                    if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
                        return Result.<User, UserCommandFailure>failure(new UserCommandFailure.WrongPassword());
                    }
                    user.setPasswordHash(passwordEncoder.encode(command.newPassword()));
                    return Result.<User, UserCommandFailure>success(userRepository.save(user));
                })
                .orElse(Result.failure(new UserCommandFailure.NotFound()));
    }
}
