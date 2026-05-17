package com.together.util;

import com.together.domain.user.User;
import com.together.dto.user.UserDto;

import java.time.Duration;
import java.time.Instant;

public final class UserMapper {

    private UserMapper() {}

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getInitials(),
                user.getAvatarColor(),
                user.getAvatarTextColor(),
                user.getRole(),
                formatLastSeen(user.getLastSeenAt())
        );
    }

    private static String formatLastSeen(Instant lastSeenAt) {
        if (lastSeenAt == null) return null;
        Duration diff = Duration.between(lastSeenAt, Instant.now());
        long minutes = diff.toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = diff.toHours();
        if (hours < 24) return hours + "h ago";
        long days = diff.toDays();
        if (days == 1) return "yesterday";
        if (days < 7) return days + "d ago";
        return lastSeenAt.toString().substring(0, 10);
    }
}
