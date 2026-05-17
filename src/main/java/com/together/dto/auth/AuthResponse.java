package com.together.dto.auth;

import com.together.dto.user.UserDto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserDto user
) {}
