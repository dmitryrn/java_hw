package org.example.java_8;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MessageService {
    private static final int MAX_PAGE_SIZE = 100;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Message " + id + " was not found"));
        return MessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageTransactional(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Message " + id + " was not found"));
        // Access the LAZY association and map it while the persistence context is open.
        message.getAuthor().getUsername();
        return MessageResponse.from(message);
    }

    // The repository entity graph loads author in the query, so this method needs no transaction.
    public MessageResponse getMessageDto(Long id) {
        Message message = messageRepository.findFirstById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Message " + id + " was not found"));
        return MessageResponse.from(message);
    }

    // Intentionally not transactional: the returned entity is detached, so its lazy author
    // cannot be initialized by the controller after this method returns.
    public Message getMessageToFail(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Message " + id + " was not found"));
    }

    @Transactional(readOnly = true)
    public MessagePage getMessages(int page, int size, String author) {
        validatePage(page, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Message> result = author == null || author.isBlank()
                ? messageRepository.findAll(pageable)
                : messageRepository.findByAuthor_Username(author, pageable);
        return new MessagePage(result.getContent().stream().map(MessageResponse::from).toList(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getLatestMessagesNPlus1() {
        List<Message> messages = messageRepository.findTop30ByCreatedAtIsNotNullOrderByCreatedAtDesc();
        // Accessing each lazy author triggers one extra SELECT per distinct author.
        messages.forEach(message -> message.getAuthor().getUsername());
        return messages.stream().map(MessageResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getLatestMessagesOptimized() {
        return messageRepository.findTop30ByOrderByCreatedAtDesc().stream()
                .map(MessageResponse::from)
                .toList();
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
        return messageRepository.saveAll(messages).size();
    }

    private Message toMessage(MessageRequest request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is required");
        }
        String username = request.author() == null || request.author().isBlank()
                ? "Anonymous" : request.author().trim();
        User author = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username)));
        return new Message(request.content(), author);
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page must be non-negative and size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }
}
