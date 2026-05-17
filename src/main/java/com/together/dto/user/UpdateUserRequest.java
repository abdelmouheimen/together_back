package com.together.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String name,
        @NotBlank String avatarColor
) {}
