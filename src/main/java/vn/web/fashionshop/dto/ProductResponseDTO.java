package vn.web.fashionshop.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String sku;
    private String productName;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private String mainImageUrl;
    private Boolean isActive;
}
