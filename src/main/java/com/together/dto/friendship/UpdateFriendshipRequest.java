package com.together.dto.friendship;

import com.together.domain.friendship.FriendshipStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateFriendshipRequest(
        @NotNull FriendshipStatus status
) {}
