package org.example.java_8;

import org.springframework.http.HttpStatus;
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

    @GetMapping("/{id}")
    public MessageResponse getMessage(@PathVariable Long id) {
        return MessageResponse.from(service.getMessage(id));
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
