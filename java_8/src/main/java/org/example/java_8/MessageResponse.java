package org.example.java_8;

import java.time.LocalDateTime;

public record MessageResponse(Long id, String content, String author, LocalDateTime createdAt) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(message.getId(), message.getContent(),
                message.getAuthor().getUsername(), message.getCreatedAt());
    }
}
