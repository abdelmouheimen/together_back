package com.together.dto.activity;

import com.together.domain.activity.ActivityType;
import com.together.dto.user.UserDto;
import java.time.Instant;
import java.util.UUID;

public record ActivityEventDto(
        UUID id,
        ActivityType type,
        UserDto actor,
        UUID listId,
        String listName,
        UUID itemId,
        String itemText,
        String extraText,
        Instant createdAt
) {}
