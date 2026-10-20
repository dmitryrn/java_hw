package com.example.mediaportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EpisodeRequest(
        @NotNull @Positive Integer number,
        @NotBlank String title,
        @NotNull @Positive Integer durationSeconds
) {
}
