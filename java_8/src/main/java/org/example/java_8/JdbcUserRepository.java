package org.example.java_8;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcUserRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findOrCreate(String username) {
        Optional<User> existing = findByUsername(username);
        if (existing.isPresent()) return existing.get();

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO users (username) VALUES (?)", new String[]{"id"});
            statement.setString(1, username);
            return statement;
        }, keyHolder);

        User user = new User(username);
        if (keyHolder.getKey() != null) user.setId(keyHolder.getKey().longValue());
        return user;
    }

    public Optional<User> findByUsername(String username) {
        return jdbcTemplate.query("SELECT id, username FROM users WHERE username = ?",
                (rs, rowNum) -> {
                    User user = new User(rs.getString("username"));
                    user.setId(rs.getLong("id"));
                    return user;
                }, username).stream().findFirst();
    }
}
