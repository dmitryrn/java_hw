package org.example.java_8;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private String content;
    private User author;
    private LocalDateTime createdAt;

    public Message() {}

    public Message(String content, User author) {
        this.content = content;
        this.author = author;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
