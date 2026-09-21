package com.bookstore.controller;

import com.bookstore.dto.AddressDto;
import com.bookstore.dto.OrderDto;
import com.bookstore.entity.Address;
import com.bookstore.entity.User;
import com.bookstore.repository.AddressRepository;
import com.bookstore.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AddressRepository addressRepository;
    private final AuthController authController;

    @GetMapping("/orders")
    public ResponseEntity<OrderDto.OrderPage> history(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.ok(orderService.getHistory(user, page, size));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDto.OrderResponse> place(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody OrderDto.PlaceOrderRequest req) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(user, req));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDto.OrderResponse> get(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.ok(orderService.getById(user, id));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderDto.OrderResponse> cancel(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.ok(orderService.cancelOrder(user, id));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto.AddressResponse>> listAddresses(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.ok(
            addressRepository.findByUserId(user.getId()).stream()
                .map(this::toAddressResponse).toList());
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDto.AddressResponse> addAddress(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody AddressDto.AddressRequest req) {
        User user = authController.resolveUser(auth);
        Address address = Address.builder()
                .user(user)
                .fullName(req.getFullName())
                .line1(req.getLine1())
                .line2(req.getLine2())
                .city(req.getCity())
                .postcode(req.getPostcode())
                .country(req.getCountry())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toAddressResponse(addressRepository.save(address)));
    }

    private AddressDto.AddressResponse toAddressResponse(Address a) {
        AddressDto.AddressResponse r = new AddressDto.AddressResponse();
        r.setId(a.getId()); r.setFullName(a.getFullName());
        r.setLine1(a.getLine1()); r.setLine2(a.getLine2());
        r.setCity(a.getCity()); r.setPostcode(a.getPostcode()); r.setCountry(a.getCountry());
        return r;
    }
}
