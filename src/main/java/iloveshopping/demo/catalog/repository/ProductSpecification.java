package iloveshopping.demo.catalog.repository;
import iloveshopping.demo.catalog.dto.ProductSearchCriteria;
import iloveshopping.demo.catalog.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> buildSpecification(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Пошук по назві та опису (Query string)
            if (StringUtils.hasText(criteria.query())) {
                String pattern = "%" + criteria.query().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (criteria.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), criteria.categoryId()));
            }

            if (criteria.brandIds() != null && !criteria.brandIds().isEmpty()) {
                predicates.add(root.get("brand").get("id").in(criteria.brandIds()));
            }

            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }

            if (criteria.minRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), criteria.minRating()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}