package iloveshopping.demo.cart.controller;

import iloveshopping.demo.cart.dto.*;
import iloveshopping.demo.cart.service.CartService;
import iloveshopping.demo.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId) {
        return ResponseEntity.ok(cartService.getCart(user, guestSessionId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(user, guestSessionId, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(user, guestSessionId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(user, guestSessionId, itemId));
    }

    @PostMapping("/merge")
    public ResponseEntity<Void> mergeCart(
            @AuthenticationPrincipal User user,
            @RequestHeader("X-Guest-Session-Id") String guestSessionId) {
        cartService.mergeCarts(user, guestSessionId);
        return ResponseEntity.ok().build();
    }
}