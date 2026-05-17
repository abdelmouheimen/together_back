package com.together.dto.item;

import jakarta.validation.constraints.NotBlank;

public record CreateItemRequest(
        @NotBlank String text
) {}
