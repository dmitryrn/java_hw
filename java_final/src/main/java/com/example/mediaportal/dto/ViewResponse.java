package com.example.mediaportal.dto;

import java.util.UUID;

public record ViewResponse(
        UUID contentId,
        long totalViews
) {
}
