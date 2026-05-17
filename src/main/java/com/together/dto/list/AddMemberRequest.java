package com.together.dto.list;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddMemberRequest(
        @NotNull UUID userId
) {}
