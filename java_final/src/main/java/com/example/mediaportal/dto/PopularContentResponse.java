package com.example.mediaportal.dto;

public record PopularContentResponse(
        ContentResponse content,
        long views
) {
}
