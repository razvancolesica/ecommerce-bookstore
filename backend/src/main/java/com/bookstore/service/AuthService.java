package com.bookstore.service;

import com.bookstore.dto.AuthDto;
import com.bookstore.dto.UserDto;
import com.bookstore.entity.User;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        }
        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(req.getPassword()) // plain for demo — no security dep
                .giftPoints(100)
                .role("USER")
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!req.getPassword().equals(user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return buildAuthResponse(user);
    }

    private AuthDto.AuthResponse buildAuthResponse(User user) {
        // Simple token: userId encoded as UUID-style string — no crypto needed for demo
        String token = "user-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(token);
        UserDto.Profile profile = new UserDto.Profile();
        profile.setId(user.getId());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEmail(user.getEmail());
        profile.setGiftPoints(user.getGiftPoints());
        response.setUser(profile);
        return response;
    }
}
