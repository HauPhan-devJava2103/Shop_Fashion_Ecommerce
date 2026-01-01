package vn.web.fashionshop.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductForm {
    private Long id;

    private String sku;
    private String productName;
    private String description;
    private Integer stock;
    private BigDecimal price;
    private BigDecimal discount;
    private Boolean isActive;
    private Long categoryId;

    // Image URLs (simple input fields)
    private String mainImageUrl;
    private String extraImageUrl1;
    private String extraImageUrl2;
    private String extraImageUrl3;
    private String extraImageUrl4;
}
