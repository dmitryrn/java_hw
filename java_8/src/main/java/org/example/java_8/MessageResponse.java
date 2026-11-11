package org.example.java_8;

public record MessageResponse(Long id, String content, String authorUsername) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(message.getId(), message.getContent(),
                message.getAuthor().getUsername());
    }
}
