package com.domoticore.iam.application;

import com.domoticore.iam.domain.User;
import com.domoticore.iam.infrastructure.UserRepository;
import com.domoticore.iam.presentation.dto.LoginRequest;
import com.domoticore.iam.presentation.dto.RegisterRequest;
import com.domoticore.shared.exception.ConflictException;
import com.domoticore.shared.exception.UnauthorizedException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User demoUser;

    @BeforeEach
    void setUp() {
        demoUser = new User();
        demoUser.setId(1L);
        demoUser.setName("Admin DomotiCore");
        demoUser.setEmail("admin@domoticore.local");
        demoUser.setPasswordHash("hashed");
        demoUser.setRole("Admin");
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        when(userRepository.findByEmailIgnoreCase("admin@domoticore.local")).thenReturn(Optional.of(demoUser));
        when(passwordEncoder.matches("SecurePass123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("admin@domoticore.local", "SecurePass123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("admin@domoticore.local");
        assertThat(response.user().name()).isEqualTo("Admin DomotiCore");
    }

    @Test
    void loginFailsForInvalidPassword() {
        when(userRepository.findByEmailIgnoreCase("admin@domoticore.local")).thenReturn(Optional.of(demoUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@domoticore.local", "wrong")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void registerFailsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase("admin@domoticore.local")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Admin", "admin@domoticore.local", "SecurePass123")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        when(userRepository.existsByEmailIgnoreCase("new@domoticore.local")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        var response = authService.register(new RegisterRequest("New User", "new@domoticore.local", "SecurePass123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("new@domoticore.local");
        verify(userRepository).save(any(User.class));
    }
}
