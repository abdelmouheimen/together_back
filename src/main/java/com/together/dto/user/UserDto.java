package com.together.dto.user;

import com.together.domain.user.UserRole;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String name,
        String initials,
        String avatarColor,
        String avatarTextColor,
        UserRole role,
        String lastSeenAt
) {}
