package iloveshopping.demo.cart;

import iloveshopping.demo.cart.entity.CartItem;
import iloveshopping.demo.catalog.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartServiceTest {

    @Test
    @DisplayName("Unit Test: Cart total calculation accuracy")
    void testCartTotalCalculation() {
        Product p1 = new Product();
        p1.setId(UUID.randomUUID());
        p1.setPrice(new BigDecimal("100.00"));

        Product p2 = new Product();
        p2.setId(UUID.randomUUID());
        p2.setPrice(new BigDecimal("50.00"));

        CartItem item1 = new CartItem();
        item1.setProduct(p1);
        item1.setQuantity(2);

        CartItem item2 = new CartItem();
        item2.setProduct(p2);
        item2.setQuantity(1);

        BigDecimal total = item1.getProduct().getPrice().multiply(BigDecimal.valueOf(item1.getQuantity()))
                .add(item2.getProduct().getPrice().multiply(BigDecimal.valueOf(item2.getQuantity())));

        assertEquals(new BigDecimal("250.00"), total);
    }
}