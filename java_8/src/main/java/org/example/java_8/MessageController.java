package org.example.java_8;

import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping
    public MessagePage getMessages(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "30") int size,
                                   @RequestParam(required = false) String author) {
        return service.getMessages(page, size, author);
    }

    @GetMapping("/nplus1")
    public List<MessageResponse> getMessagesNPlus1() {
        return service.getLatestMessagesNPlus1();
    }

    @GetMapping("/optimized")
    public List<MessageResponse> getMessagesOptimized() {
        return service.getLatestMessagesOptimized();
    }

    @GetMapping("/{id}")
    public MessageResponse getMessage(@PathVariable Long id) {
        return service.getMessage(id);
    }

    @GetMapping("/{id}/transactional")
    public MessageResponse getMessageTransactional(@PathVariable Long id) {
        return service.getMessageTransactional(id);
    }

    @GetMapping("/{id}/dto")
    public MessageResponse getMessageDto(@PathVariable Long id) {
        return service.getMessageDto(id);
    }

    @GetMapping("/fail/{id}")
    public ResponseEntity<String> getMessageToFail(@PathVariable Long id) {
        Message message = service.getMessageToFail(id);
        try {
            // Force initialization of the detached LAZY proxy to demonstrate the failure.
            message.getAuthor().getUsername();
            return ResponseEntity.ok("Unexpectedly loaded author: " + message.getAuthor().getUsername());
        } catch (LazyInitializationException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("LazyInitializationException: " + exception.getMessage());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createMessage(@RequestBody MessageRequest request) {
        return service.saveMessage(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public BatchCreateResponse createMessages(@RequestBody List<MessageRequest> requests) {
        return new BatchCreateResponse(service.saveMessages(requests));
    }

    public record BatchCreateResponse(int created) {}
}
