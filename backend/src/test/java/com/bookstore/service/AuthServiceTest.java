package com.bookstore.service;

import com.bookstore.dto.AuthDto;
import com.bookstore.entity.User;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private AuthService authService;

    private AuthDto.RegisterRequest registerRequest;
    private AuthDto.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new AuthDto.RegisterRequest();
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("jane@example.com");
        registerRequest.setPassword("secret123");

        loginRequest = new AuthDto.LoginRequest();
        loginRequest.setEmail("jane@example.com");
        loginRequest.setPassword("secret123");
    }

    @Test
    @DisplayName("register - should create user and return token")
    void register_success() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                    .id(1L).firstName(u.getFirstName()).lastName(u.getLastName())
                    .email(u.getEmail()).password(u.getPassword())
                    .giftPoints(100).role("USER").build();
            return u;
        });

        AuthDto.AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isNotNull().startsWith("user-");
        assertThat(response.getUser().getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getUser().getGiftPoints()).isEqualTo(100);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - should throw CONFLICT when email already exists")
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    @DisplayName("login - should return token for valid credentials")
    void login_success() {
        User user = User.builder().id(1L).firstName("Jane").lastName("Doe")
                .email("jane@example.com").password("secret123")
                .giftPoints(50).role("USER").build();
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        AuthDto.AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).startsWith("user-1-");
        assertThat(response.getUser().getFirstName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("login - should throw UNAUTHORIZED for wrong password")
    void login_wrongPassword_throwsUnauthorized() {
        User user = User.builder().id(1L).email("jane@example.com")
                .password("differentpassword").giftPoints(0).role("USER").build();
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("login - should throw UNAUTHORIZED for unknown email")
    void login_unknownEmail_throwsUnauthorized() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid credentials");
    }
}
