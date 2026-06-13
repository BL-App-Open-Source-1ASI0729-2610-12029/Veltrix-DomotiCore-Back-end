package com.domoticore.iam.application.internal.commandservices;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.commands.LoginUserCommand;
import com.domoticore.iam.domain.model.commands.RegisterUserCommand;
import com.domoticore.iam.domain.model.valueobjects.Email;
import com.domoticore.iam.infrastructure.persistence.jpa.UserRepository;
import com.domoticore.settings.application.UserProfileService;
import com.domoticore.shared.application.result.Result;
import com.domoticore.shared.exception.ConflictException;
import com.domoticore.shared.exception.UnauthorizedException;
import com.domoticore.iam.interfaces.transform.UserResponseAssembler;
import com.domoticore.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    private User demoUser;

    @BeforeEach
    void setUp() {
        demoUser = User.newEmpty();
        demoUser.setId(1L);
        demoUser.setName("Admin DomotiCore");
        demoUser.setEmail("admin@domoticore.local");
        demoUser.setPasswordHash("hashed");
        demoUser.setRole("Admin");
    }

    @Test
    void loginReturnsUserForValidCredentials() {
        when(userRepository.findByEmailAddress(new Email("admin@domoticore.local"))).thenReturn(Optional.of(demoUser));
        when(passwordEncoder.matches("SecurePass123", "hashed")).thenReturn(true);

        Result<User, ?> result = userCommandService.login(
                new LoginUserCommand(new Email("admin@domoticore.local"), "SecurePass123"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.fold(User::getEmail, failure -> null)).isEqualTo("admin@domoticore.local");
    }

    @Test
    void loginFailsForInvalidPassword() {
        when(userRepository.findByEmailAddress(new Email("admin@domoticore.local"))).thenReturn(Optional.of(demoUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Result<User, ?> result = userCommandService.login(
                new LoginUserCommand(new Email("admin@domoticore.local"), "wrong"));

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void registerFailsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmailAddress(new Email("admin@domoticore.local"))).thenReturn(true);

        Result<User, ?> result = userCommandService.register(
                new RegisterUserCommand("Admin", new Email("admin@domoticore.local"), "SecurePass123"));

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void registerCreatesUser() {
        when(userRepository.existsByEmailAddress(new Email("new@domoticore.local"))).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Result<User, ?> result = userCommandService.register(
                new RegisterUserCommand("New User", new Email("new@domoticore.local"), "SecurePass123"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.fold(User::getEmail, failure -> null)).isEqualTo("new@domoticore.local");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void assemblerMapsDuplicateToConflictException() {
        JwtService jwtService = mock(JwtService.class);
        UserResponseAssembler assembler = new UserResponseAssembler(jwtService);

        assertThatThrownBy(() -> assembler.toAuthResponse(
                Result.failure(new com.domoticore.iam.application.commandservices.UserCommandFailure.Duplicate())))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void assemblerMapsInvalidCredentialsToUnauthorizedException() {
        JwtService jwtService = mock(JwtService.class);
        UserResponseAssembler assembler = new UserResponseAssembler(jwtService);

        assertThatThrownBy(() -> assembler.toAuthResponse(
                Result.failure(new com.domoticore.iam.application.commandservices.UserCommandFailure.InvalidCredentials())))
                .isInstanceOf(UnauthorizedException.class);
    }
}
