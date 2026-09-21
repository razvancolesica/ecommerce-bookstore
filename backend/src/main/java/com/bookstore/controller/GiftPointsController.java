package com.bookstore.controller;

import com.bookstore.entity.User;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/gift-points")
@RequiredArgsConstructor
public class GiftPointsController {

    private static final BigDecimal POINT_VALUE = new BigDecimal("0.01");
    private final AuthController authController;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBalance(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = authController.resolveUser(auth);
        BigDecimal value = POINT_VALUE.multiply(BigDecimal.valueOf(user.getGiftPoints()));
        return ResponseEntity.ok(Map.of(
                "balance", user.getGiftPoints(),
                "equivalentValue", value));
    }
}
