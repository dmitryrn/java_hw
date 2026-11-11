package org.example.java_8;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MessageService {
    private static final int MAX_PAGE_SIZE = 100;

    private final JdbcMessageRepository messageRepository;
    private final JdbcUserRepository userRepository;

    public MessageService(JdbcMessageRepository messageRepository, JdbcUserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public Message getMessage(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Message " + id + " was not found"));
    }

    public MessagePage getMessages(int page, int size, String author) {
        validatePage(page, size);
        int offset = page * size;
        boolean filterByAuthor = author != null && !author.isBlank();
        List<Message> content = filterByAuthor
                ? messageRepository.findPageByAuthor(author, size, offset)
                : messageRepository.findPage(size, offset);
        long total = filterByAuthor
                ? messageRepository.countByAuthor(author)
                : messageRepository.count();
        return new MessagePage(content.stream().map(MessageResponse::from).toList(),
                page, size, total, (int) Math.ceil((double) total / size));
    }

    @Transactional
    public MessageResponse saveMessage(MessageRequest request) {
        return MessageResponse.from(messageRepository.save(toMessage(request)));
    }

    @Transactional
    public int saveMessages(List<MessageRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one message is required");
        }
        List<Message> messages = requests.stream().map(this::toMessage).toList();
        return messageRepository.saveAll(messages);
    }

    private Message toMessage(MessageRequest request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is required");
        }
        String username = request.author() == null || request.author().isBlank()
                ? "Anonymous" : request.author().trim();
        return new Message(request.content(), userRepository.findOrCreate(username));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE || page > Integer.MAX_VALUE / size) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page must be non-negative and size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }
}
