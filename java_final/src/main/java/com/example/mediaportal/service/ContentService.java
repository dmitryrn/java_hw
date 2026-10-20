package com.example.mediaportal.service;

import com.example.mediaportal.dto.ContentResponse;
import com.example.mediaportal.dto.CreateContentRequest;
import com.example.mediaportal.dto.EpisodeRequest;
import com.example.mediaportal.dto.PopularContentResponse;
import com.example.mediaportal.dto.ViewResponse;
import com.example.mediaportal.entity.Content;
import com.example.mediaportal.entity.ContentType;
import com.example.mediaportal.entity.Episode;
import com.example.mediaportal.exception.BadRequestException;
import com.example.mediaportal.exception.ResourceNotFoundException;
import com.example.mediaportal.repository.ContentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContentService {

    private final ContentRepository contentRepository;
    private final AnalyticsService analyticsService;

    public ContentService(ContentRepository contentRepository, AnalyticsService analyticsService) {
        this.contentRepository = contentRepository;
        this.analyticsService = analyticsService;
    }

    public ContentResponse create(CreateContentRequest request) {
        validate(request);

        Content content = new Content();
        applyRequest(content, request);
        return ContentResponse.from(contentRepository.save(content));
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> findAll(ContentType type) {
        List<Content> content = type == null
                ? contentRepository.findAllByOrderByCreatedAtDesc()
                : contentRepository.findAllByTypeOrderByCreatedAtDesc(type);
        return content.stream().map(ContentResponse::from).toList();
    }

    @Cacheable(cacheNames = "content", key = "#id")
    @Transactional(readOnly = true)
    public ContentResponse findById(UUID id) {
        return contentRepository.findById(id)
                .map(ContentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + id));
    }

    @CacheEvict(cacheNames = "content", key = "#id")
    public ContentResponse update(UUID id, CreateContentRequest request) {
        validate(request);

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + id));
        applyRequest(content, request);
        return ContentResponse.from(contentRepository.save(content));
    }

    @CacheEvict(cacheNames = "content", key = "#id")
    public void delete(UUID id) {
        if (!contentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Content not found: " + id);
        }
        contentRepository.deleteById(id);
    }

    public ViewResponse registerView(UUID id) {
        if (!contentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Content not found: " + id);
        }
        return new ViewResponse(id, analyticsService.registerView(id));
    }

    @Transactional(readOnly = true)
    public List<PopularContentResponse> popular(int limit) {
        return analyticsService.topContent(limit).entrySet().stream()
                .map(entry -> {
                    try {
                        return new PopularContentResponse(findById(entry.getKey()), entry.getValue());
                    } catch (ResourceNotFoundException exception) {
                        return null;
                    }
                })
                .filter(response -> response != null)
                .toList();
    }

    private void validate(CreateContentRequest request) {
        if (request.type() == ContentType.ARTICLE) {
            requireText(request.text(), "Article text is required");
            requireText(request.author(), "Article author is required");
            if (request.publicationDate() == null) {
                throw new BadRequestException("Article publicationDate is required");
            }
            return;
        }

        if (request.type() == ContentType.VIDEO) {
            requireText(request.url(), "Video url is required");
            requirePositive(request.durationSeconds(), "Video durationSeconds must be positive");
            return;
        }

        if (request.type() == ContentType.PODCAST) {
            requireText(request.audioUrl(), "Podcast audioUrl is required");
            if (request.episodes() == null || request.episodes().isEmpty()) {
                throw new BadRequestException("Podcast episodes are required");
            }
        }
    }

    private void applyRequest(Content content, CreateContentRequest request) {
        content.setType(request.type());
        content.setTitle(request.title().trim());
        content.setText(null);
        content.setAuthor(null);
        content.setPublicationDate(null);
        content.setUrl(null);
        content.setAudioUrl(null);
        content.setDurationSeconds(null);
        content.setEpisodes(new ArrayList<>());

        switch (request.type()) {
            case ARTICLE -> {
                content.setText(request.text());
                content.setAuthor(request.author());
                content.setPublicationDate(request.publicationDate());
            }
            case VIDEO -> {
                content.setUrl(request.url());
                content.setDurationSeconds(request.durationSeconds());
            }
            case PODCAST -> {
                content.setAudioUrl(request.audioUrl());
                content.setEpisodes(toEpisodes(request.episodes()));
            }
        }
    }

    private List<Episode> toEpisodes(List<EpisodeRequest> episodes) {
        return episodes.stream()
                .map(episode -> new Episode(
                        episode.number(),
                        episode.title(),
                        episode.durationSeconds()))
                .toList();
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
    }

    private void requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new BadRequestException(message);
        }
    }
}
