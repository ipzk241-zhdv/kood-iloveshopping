package iloveshopping.demo.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal priceAtPurchase,
        int quantity,
        BigDecimal subTotal
) {}