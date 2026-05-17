package com.together.dto.friendship;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendFriendRequestDto(
        @NotNull UUID addresseeId
) {}
