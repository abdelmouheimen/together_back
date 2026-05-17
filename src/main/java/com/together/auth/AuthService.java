package com.together.auth;

import com.together.domain.user.User;
import com.together.domain.user.UserRepository;
import com.together.domain.user.UserRole;
import com.together.dto.auth.AuthResponse;
import com.together.dto.auth.LoginRequest;
import com.together.dto.auth.RefreshTokenRequest;
import com.together.dto.auth.RegisterRequest;
import com.together.exception.ConflictException;
import com.together.util.AvatarUtils;
import com.together.util.UserMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setInitials(AvatarUtils.generateInitials(request.name()));
        user.setAvatarColor(request.avatarColor());
        user.setAvatarTextColor(AvatarUtils.computeTextColor(request.avatarColor()));
        user.setRole(UserRole.FRIEND);
        user.setCreatedAt(Instant.now());

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UsernameNotFoundException("Email ou mot de passe incorrect");
        }

        user.setLastSeenAt(Instant.now());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        if (!tokenProvider.isTokenValid(token)) {
            throw new UsernameNotFoundException("Invalid or expired refresh token");
        }

        UUID userId = tokenProvider.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newAccessToken = tokenProvider.generateAccessToken(user);
        return new AuthResponse(newAccessToken, token, UserMapper.toDto(user));
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, UserMapper.toDto(user));
    }
}
