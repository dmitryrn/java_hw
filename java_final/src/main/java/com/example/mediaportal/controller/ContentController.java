package com.example.mediaportal.controller;

import com.example.mediaportal.dto.ContentResponse;
import com.example.mediaportal.dto.CreateContentRequest;
import com.example.mediaportal.dto.PopularContentResponse;
import com.example.mediaportal.dto.ViewResponse;
import com.example.mediaportal.entity.ContentType;
import com.example.mediaportal.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/content")
@Validated
@Tag(name = "Content", description = "Content management and popularity API")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    @Operation(summary = "Create content")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ContentResponse> create(
            @Valid @RequestBody CreateContentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.create(request));
    }

    @GetMapping
    @Operation(summary = "List content")
    public List<ContentResponse> findAll(
            @RequestParam(required = false) ContentType type) {
        return contentService.findAll(type);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get the most-viewed content for the current hour")
    public List<PopularContentResponse> popular(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return contentService.popular(limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get content by ID")
    public ContentResponse findById(@PathVariable UUID id) {
        return contentService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace content")
    @SecurityRequirement(name = "bearerAuth")
    public ContentResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateContentRequest request) {
        return contentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete content")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/views")
    @Operation(summary = "Record a content view")
    public ViewResponse registerView(@PathVariable UUID id) {
        return contentService.registerView(id);
    }
}
