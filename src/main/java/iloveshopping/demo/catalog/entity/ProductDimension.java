package iloveshopping.demo.catalog.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDimension {
    private Double weightKg;
    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;

    private Double weightLbs;
    private Double lengthInches;
    private Double widthInches;
    private Double heightInches;
}