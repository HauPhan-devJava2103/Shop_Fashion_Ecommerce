package vn.web.fashionshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryForm {

    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    @NotBlank(message = "Slug không được để trống")
    private String slug;

    private String imageUrl;

    private Long parentId;

    private Boolean isActive = true;
}
