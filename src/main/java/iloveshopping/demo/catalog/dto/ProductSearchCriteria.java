package iloveshopping.demo.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductSearchCriteria(
        String query,
        UUID categoryId,
        List<UUID> brandIds,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Double minRating,
        String sortBy,
        int page,
        int size
) {
    public ProductSearchCriteria {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
    }
}