package iloveshopping.demo.catalog.service;

import iloveshopping.demo.catalog.dto.ProductSearchCriteria;
import iloveshopping.demo.catalog.entity.Product;
import iloveshopping.demo.catalog.repository.ProductRepository;
import iloveshopping.demo.catalog.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(ProductSearchCriteria criteria) {
        Sort sort = switch (criteria.sortBy() != null ? criteria.sortBy() : "relevance") {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating" -> Sort.by("averageRating").descending();
            default -> Sort.by("name").ascending();
        };

        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);
        return productRepository.findAll(ProductSpecification.buildSpecification(criteria), pageable);
    }

    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return productRepository.findTopSuggestions(query.trim(), PageRequest.of(0, 5));
    }
}