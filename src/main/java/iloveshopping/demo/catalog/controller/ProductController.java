package iloveshopping.demo.catalog.controller;

import iloveshopping.demo.catalog.dto.ProductSearchCriteria;
import iloveshopping.demo.catalog.entity.Product;
import iloveshopping.demo.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(ProductSearchCriteria criteria) {
        return ResponseEntity.ok(productService.searchProducts(criteria));
    }
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam("q") String query) {
        return ResponseEntity.ok(productService.getSearchSuggestions(query));
    }
}