package iloveshopping.demo.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        java.util.UUID productId,
        String productName,
        BigDecimal unitPrice,
        Object thumbnailUrl,
        int quantity,
        BigDecimal subTotal
) {}