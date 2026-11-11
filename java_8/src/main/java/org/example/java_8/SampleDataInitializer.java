package org.example.java_8;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SampleDataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public SampleDataInitializer(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (messageRepository.count() > 0) return;

        User alice = userRepository.save(new User("Alice"));
        User bob = userRepository.save(new User("Bob"));
        messageRepository.save(new Message("Hello, JPA and Hibernate!", alice));
        messageRepository.save(new Message("This uses Spring Data JPA.", bob));
        messageRepository.save(new Message("Relationships are managed by ORM.", alice));
    }
}
