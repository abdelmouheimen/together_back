package com.together.dto.item;

import jakarta.validation.constraints.NotBlank;

public record UpdateItemRequest(
        @NotBlank String text
) {}
