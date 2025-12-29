package vn.web.fashionshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductVariantUpdateDTO {

    private Long id; // null for new variant

    @Size(max = 50, message = "SKU variant không được quá 50 ký tự")
    private String skuVariant;

    @Size(max = 5, message = "Size không được quá 5 ký tự")
    private String size; // XS, S, M, L, XL, XXL

    @Size(max = 50, message = "Color không được quá 50 ký tự")
    private String color;

    @Min(value = 0, message = "Tồn kho phải >= 0")
    private Integer stock;

    private Boolean _delete; // Flag for deletion
}
