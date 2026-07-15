package com.together.dto.list;

import com.together.dto.user.UserDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TodoListDto(
        UUID id,
        String name,
        String emoji,
        String accentColor,
        String progressColor,
        List<UserDto> members,
        int totalItems,
        int doneItems,
        double progress,
        UUID parentId,
        Instant createdAt
) {}
