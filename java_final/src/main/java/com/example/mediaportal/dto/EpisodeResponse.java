package com.example.mediaportal.dto;

import com.example.mediaportal.entity.Episode;

public record EpisodeResponse(
        Integer number,
        String title,
        Integer durationSeconds
) {
    public static EpisodeResponse from(Episode episode) {
        return new EpisodeResponse(
                episode.getNumber(),
                episode.getTitle(),
                episode.getDurationSeconds()
        );
    }
}
