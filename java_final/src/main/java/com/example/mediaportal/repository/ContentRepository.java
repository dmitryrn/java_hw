package com.example.mediaportal.repository;

import com.example.mediaportal.entity.Content;
import com.example.mediaportal.entity.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {

    List<Content> findAllByOrderByCreatedAtDesc();

    List<Content> findAllByTypeOrderByCreatedAtDesc(ContentType type);
}
