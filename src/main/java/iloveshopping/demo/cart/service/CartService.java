package iloveshopping.demo.cart.service;

import iloveshopping.demo.cart.dto.*;
import iloveshopping.demo.cart.entity.Cart;
import iloveshopping.demo.cart.entity.CartItem;
import iloveshopping.demo.cart.repository.CartItemRepository;
import iloveshopping.demo.cart.repository.CartRepository;
import iloveshopping.demo.catalog.entity.Product;
import iloveshopping.demo.catalog.repository.ProductRepository;
import iloveshopping.demo.user.entity.User;
import iloveshopping.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(User user, String guestSessionId) {
        Cart cart = getOrCreateCartEntity(user, guestSessionId);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(User user, String guestSessionId, AddToCartRequest request) {
        Cart cart = getOrCreateCartEntity(user, guestSessionId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + request.productId()));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(request.quantity());
            cart.addItem(newItem);
        }

        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(User user, String guestSessionId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCartEntity(user, guestSessionId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to this cart");
        }

        item.setQuantity(request.quantity());
        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(User user, String guestSessionId, Long itemId) {
        Cart cart = getOrCreateCartEntity(user, guestSessionId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to this cart");
        }

        cart.removeItem(item);
        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public void mergeCarts(User user, String guestSessionId) {
        if (guestSessionId == null) return;

        Optional<Cart> guestCartOpt = cartRepository.findByGuestSessionId(guestSessionId);
        if (guestCartOpt.isEmpty()) return;

        Cart guestCart = guestCartOpt.get();
        Cart userCart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> userItemOpt = userCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(guestItem.getProduct().getId()))
                    .findFirst();

            if (userItemOpt.isPresent()) {
                CartItem userItem = userItemOpt.get();
                userItem.setQuantity(userItem.getQuantity() + guestItem.getQuantity());
            } else {
                CartItem newItem = new CartItem();
                newItem.setProduct(guestItem.getProduct());
                newItem.setQuantity(guestItem.getQuantity());
                userCart.addItem(newItem);
            }
        }

        cartRepository.delete(guestCart);
        cartRepository.save(userCart);
    }

    private Cart getOrCreateCartEntity(User user, String guestSessionId) {
        if (user != null) {
            return cartRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setUser(user);
                        return cartRepository.save(cart);
                    });
        }

        if (guestSessionId != null) {
            return cartRepository.findByGuestSessionId(guestSessionId)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setGuestSessionId(guestSessionId);
                        return cartRepository.save(cart);
                    });
        }

        throw new IllegalArgumentException("Either user or guestSessionId must be provided");
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    BigDecimal unitPrice = item.getProduct().getPrice();
                    BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartItemResponse(
                            item.getId(),
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            unitPrice,
                            item.getProduct().getImageUrl(),
                            item.getQuantity(),
                            subTotal
                    );
                }).toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), itemResponses, totalAmount);
    }
}