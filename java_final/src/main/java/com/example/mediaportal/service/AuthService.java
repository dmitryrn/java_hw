package com.example.mediaportal.service;

import com.example.mediaportal.dto.AuthResponse;
import com.example.mediaportal.dto.LoginRequest;
import com.example.mediaportal.dto.RegisterRequest;
import com.example.mediaportal.entity.Role;
import com.example.mediaportal.entity.UserEntity;
import com.example.mediaportal.exception.ConflictException;
import com.example.mediaportal.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username is already registered");
        }

        userRepository.save(new UserEntity(
                username,
                passwordEncoder.encode(request.password()),
                Role.USER));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse(Role.USER.name());

        return new AuthResponse(
                jwtService.createToken(authentication.getName(), role),
                "Bearer");
    }
}
