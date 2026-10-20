package com.example.mediaportal.service;

import com.example.mediaportal.dto.ContentResponse;
import com.example.mediaportal.dto.CreateContentRequest;
import com.example.mediaportal.entity.Content;
import com.example.mediaportal.entity.ContentType;
import com.example.mediaportal.exception.BadRequestException;
import com.example.mediaportal.exception.ResourceNotFoundException;
import com.example.mediaportal.repository.ContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private AnalyticsService analyticsService;

    @Test
    void createsArticleWithArticleFields() {
        ContentService service = new ContentService(contentRepository, analyticsService);
        when(contentRepository.save(any(Content.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContentResponse response = service.create(new CreateContentRequest(
                ContentType.ARTICLE,
                "Spring Boot",
                "Article text",
                "Author",
                LocalDate.of(2026, 10, 20),
                null,
                null,
                null,
                null));

        assertThat(response.type()).isEqualTo(ContentType.ARTICLE);
        assertThat(response.title()).isEqualTo("Spring Boot");
        assertThat(response.text()).isEqualTo("Article text");
        assertThat(response.author()).isEqualTo("Author");
    }

    @Test
    void rejectsVideoWithoutDuration() {
        ContentService service = new ContentService(contentRepository, analyticsService);

        assertThatThrownBy(() -> service.create(new CreateContentRequest(
                ContentType.VIDEO,
                "Video",
                null,
                null,
                null,
                "https://example.com/video",
                null,
                null,
                null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("durationSeconds");
    }

    @Test
    void rejectsUnknownContent() {
        ContentService service = new ContentService(contentRepository, analyticsService);
        UUID id = UUID.randomUUID();
        when(contentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
