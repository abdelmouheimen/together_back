package com.together.dto.item;

import com.together.domain.item.ItemCategory;
import jakarta.validation.constraints.NotBlank;

public record CreateItemRequest(
        @NotBlank String text,
        ItemCategory category
) {}
