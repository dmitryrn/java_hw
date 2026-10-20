package com.example.mediaportal.dto;

import com.example.mediaportal.entity.ContentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateContentRequest(
        @NotNull ContentType type,
        @NotBlank String title,
        String text,
        String author,
        LocalDate publicationDate,
        String url,
        String audioUrl,
        Integer durationSeconds,
        List<@Valid EpisodeRequest> episodes
) {
}
