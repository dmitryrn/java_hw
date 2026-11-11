package org.example.java_8;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMessageRepository {
    private static final String SELECT_MESSAGE = """
            SELECT m.id, m.content, m.created_at, u.id AS author_id, u.username AS author_username
            FROM messages m JOIN users u ON u.id = m.author_id
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Message> messageRowMapper = (rs, rowNum) -> {
        User author = new User(rs.getString("author_username"));
        author.setId(rs.getLong("author_id"));
        Message message = new Message(rs.getString("content"), author);
        message.setId(rs.getLong("id"));
        message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return message;
    };

    public JdbcMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Message save(Message message) {
        String sql = "INSERT INTO messages (content, author_id) VALUES (?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, message.getContent());
            statement.setLong(2, message.getAuthor().getId());
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            message.setId(keyHolder.getKey().longValue());
        }
        return findById(message.getId()).orElse(message);
    }

    public int saveAll(List<Message> messages) {
        String sql = "INSERT INTO messages (content, author_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, messages, 100,
                (statement, message) -> {
                    statement.setString(1, message.getContent());
                    statement.setLong(2, message.getAuthor().getId());
                });
        return messages.size();
    }

    public Optional<Message> findById(Long id) {
        return jdbcTemplate.query(SELECT_MESSAGE + " WHERE m.id = ?", messageRowMapper, id)
                .stream().findFirst();
    }

    public List<Message> findPage(int limit, int offset) {
        return jdbcTemplate.query(SELECT_MESSAGE +
                " ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?",
                messageRowMapper, limit, offset);
    }

    public List<Message> findPageByAuthor(String username, int limit, int offset) {
        return jdbcTemplate.query(SELECT_MESSAGE +
                        " WHERE u.username = ? ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?",
                messageRowMapper, username, limit, offset);
    }

    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM messages", Long.class);
    }

    public long countByAuthor(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages m JOIN users u ON u.id = m.author_id WHERE u.username = ?",
                Long.class, username);
    }
}
