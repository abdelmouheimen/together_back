package com.together.dto.item;

import com.together.domain.item.ItemCategory;
import com.together.dto.comment.CommentDto;
import com.together.dto.user.UserDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TodoItemDto(
        UUID id,
        String text,
        boolean done,
        ItemCategory category,
        UserDto checkedBy,
        Instant checkedAt,
        int position,
        List<CommentDto> comments,
        Instant createdAt
) {}
