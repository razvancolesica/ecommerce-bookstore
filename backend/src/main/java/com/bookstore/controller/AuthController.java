package com.bookstore.controller;

import com.bookstore.dto.AuthDto;
import com.bookstore.dto.UserDto;
import com.bookstore.entity.User;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponse> register(
            @Valid @RequestBody AuthDto.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto.Profile> me(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        UserDto.Profile profile = new UserDto.Profile();
        profile.setId(user.getId());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEmail(user.getEmail());
        profile.setGiftPoints(user.getGiftPoints());
        return ResponseEntity.ok(profile);
    }

    User resolveUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer user-")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        try {
            String userId = authHeader.replace("Bearer user-", "").split("-")[0];
            return userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }
}
