package com.domoticore.iam.application;

import com.domoticore.iam.domain.AccountType;
import com.domoticore.iam.domain.User;
import com.domoticore.iam.infrastructure.UserRepository;
import com.domoticore.iam.presentation.dto.AuthResponse;
import com.domoticore.iam.presentation.dto.LoginRequest;
import com.domoticore.iam.presentation.dto.RegisterRequest;
import com.domoticore.iam.presentation.dto.UpdateUserRequest;
import com.domoticore.iam.presentation.dto.UserResponse;
import com.domoticore.shared.exception.ConflictException;
import com.domoticore.shared.exception.ResourceNotFoundException;
import com.domoticore.shared.exception.UnauthorizedException;
import com.domoticore.shared.security.DomotiCoreUserDetails;
import com.domoticore.shared.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("User");
        user.setOnboardingCompleted(false);
        user.setAvatar("https://ui-avatars.com/api/?name="
                + request.name().replace(" ", "+")
                + "&background=3455d1&color=ffffff");

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.email() != null) {
            user.setEmail(request.email().toLowerCase());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
        }
        if (request.accountType() != null) {
            user.setAccountType(AccountType.fromJson(request.accountType()));
        }
        if (request.onboardingCompleted() != null) {
            user.setOnboardingCompleted(request.onboardingCompleted());
        }

        return UserResponse.from(userRepository.save(user));
    }

    private AuthResponse buildAuthResponse(User user) {
        DomotiCoreUserDetails userDetails = new DomotiCoreUserDetails(user);
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, UserResponse.from(user));
    }
}
