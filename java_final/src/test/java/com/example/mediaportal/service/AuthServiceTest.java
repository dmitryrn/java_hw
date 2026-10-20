package com.example.mediaportal.service;

import com.example.mediaportal.dto.AuthResponse;
import com.example.mediaportal.dto.RegisterRequest;
import com.example.mediaportal.entity.Role;
import com.example.mediaportal.entity.UserEntity;
import com.example.mediaportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Test
    void registersUserWithEncodedPasswordAndUserRole() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthService service = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService);
        when(userRepository.existsByUsername("editor")).thenReturn(false);

        service.register(new RegisterRequest(" editor ", "password123"));

        var captor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("editor");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(passwordEncoder.matches("password123", captor.getValue().getPassword())).isTrue();
    }

    @Test
    void returnsJwtAfterSuccessfulLogin() {
        AuthService service = new AuthService(
                userRepository,
                new BCryptPasswordEncoder(),
                authenticationManager,
                jwtService);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("editor");
        doReturn(java.util.List.of(
                (GrantedAuthority) new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication).getAuthorities();

        when(jwtService.createToken("editor", "USER")).thenReturn("jwt-token");

        AuthResponse response = service.login(new com.example.mediaportal.dto.LoginRequest(
                "editor", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
