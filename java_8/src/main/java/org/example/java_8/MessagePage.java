package org.example.java_8;

import java.util.List;

public record MessagePage(List<MessageResponse> content, int page, int size,
                         long totalElements, int totalPages) {}
