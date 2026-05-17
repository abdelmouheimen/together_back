package com.together.dto.item;

import java.util.UUID;

public record ReorderItemRequest(
        UUID id,
        int position
) {}
