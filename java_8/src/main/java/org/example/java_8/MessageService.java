package org.example.java_8;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private static final int MESSAGE_LIMIT = 30;
    private final JdbcMessageRepository repository;

    public MessageService(JdbcMessageRepository repository) {
        this.repository = repository;
        initializeSampleData();
    }

    private void initializeSampleData() {
        repository.clear();
        repository.save(new Message("Hello, JDBC Spring!", "System"));
        repository.save(new Message("Now using H2 database via JdbcTemplate.", "Admin"));
    }

    public List<Message> getLatestMessages() {
        return repository.findLatest(MESSAGE_LIMIT);
    }

    public Message saveMessage(Message message) {
        return repository.save(message);
    }
}
