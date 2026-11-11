package org.example.java_8;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMessageRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Message> messageRowMapper = (rs, rowNum) -> {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setContent(rs.getString("content"));
        message.setAuthor(rs.getString("author"));
        message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return message;
    };

    public JdbcMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Message save(Message message) {
        String sql = "INSERT INTO messages (content, author) VALUES (?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, message.getContent());
            statement.setString(2, message.getAuthor());
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            message.setId(keyHolder.getKey().longValue());
        }
        return message;
    }

    public Optional<Message> findById(Long id) {
        String sql = "SELECT id, content, author, created_at FROM messages WHERE id = ?";
        List<Message> results = jdbcTemplate.query(sql, messageRowMapper, id);
        return results.stream().findFirst();
    }

    public List<Message> findLatest(int limit) {
        String sql = "SELECT id, content, author, created_at FROM messages " +
                "ORDER BY created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, messageRowMapper, limit);
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM messages WHERE id = ?", id);
    }

    public void clear() {
        jdbcTemplate.update("DELETE FROM messages");
    }
}
