package com.together.dto.comment;

import com.together.dto.user.UserDto;
import java.time.Instant;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UserDto author,
        String text,
        Instant createdAt
) {}
