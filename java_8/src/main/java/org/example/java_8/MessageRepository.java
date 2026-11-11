package org.example.java_8;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByAuthor_Username(String username, Pageable pageable);

    List<Message> findTop30ByCreatedAtIsNotNullOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "author")
    List<Message> findTop30ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "author")
    Optional<Message> findFirstById(Long id);
}
