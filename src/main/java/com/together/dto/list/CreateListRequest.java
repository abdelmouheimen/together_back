package com.together.dto.list;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record CreateListRequest(
        @NotBlank String name,
        @NotBlank String emoji,
        @NotBlank String accentColor,
        @NotBlank String progressColor,
        List<UUID> memberIds
) {}
