package com.example.mediaportal.dto;

public record AuthResponse(
        String token,
        String tokenType
) {
}
