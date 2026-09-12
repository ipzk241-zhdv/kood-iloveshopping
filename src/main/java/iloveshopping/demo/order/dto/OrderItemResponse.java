package iloveshopping.demo.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        Long id,
        UUID productId,
        String productName,
        BigDecimal priceAtPurchase,
        int quantity,
        BigDecimal subTotal
) {}
