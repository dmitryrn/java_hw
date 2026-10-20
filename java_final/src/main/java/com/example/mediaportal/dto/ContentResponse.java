package com.example.mediaportal.dto;

import com.example.mediaportal.entity.Content;
import com.example.mediaportal.entity.ContentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContentResponse(
        UUID id,
        ContentType type,
        String title,
        String text,
        String author,
        LocalDate publicationDate,
        String url,
        String audioUrl,
        Integer durationSeconds,
        List<EpisodeResponse> episodes,
        Instant createdAt,
        Instant updatedAt
) {
    public static ContentResponse from(Content content) {
        return new ContentResponse(
                content.getId(),
                content.getType(),
                content.getTitle(),
                content.getText(),
                content.getAuthor(),
                content.getPublicationDate(),
                content.getUrl(),
                content.getAudioUrl(),
                content.getDurationSeconds(),
                content.getEpisodes().stream().map(EpisodeResponse::from).toList(),
                content.getCreatedAt(),
                content.getUpdatedAt()
        );
    }
}
