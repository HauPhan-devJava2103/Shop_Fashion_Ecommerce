package vn.web.fashionshop.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductUpdateDTO {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255)
    private String productName;

    @NotBlank(message = "SKU không được để trống")
    @Size(max = 50)
    private String sku;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0)
    private Double price;

    @Min(value = 0)
    @Max(value = 100)
    private Double discount;

    @Min(value = 0)
    private Integer stock;

    @Size(max = 500)
    private String description;

    private Boolean isActive;

    private List<ProductVariantUpdateDTO> variants;
}
