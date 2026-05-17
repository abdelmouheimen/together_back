package com.together.dto.list;

import jakarta.validation.constraints.NotBlank;

public record UpdateListRequest(
        @NotBlank String name,
        @NotBlank String emoji,
        @NotBlank String accentColor,
        @NotBlank String progressColor
) {}
