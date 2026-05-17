package com.together.dto.friendship;

import com.together.domain.friendship.FriendshipStatus;
import com.together.dto.user.UserDto;
import java.time.Instant;
import java.util.UUID;

public record FriendshipDto(
        UUID id,
        UserDto requester,
        UserDto addressee,
        FriendshipStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
