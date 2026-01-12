package vn.web.fashionshop.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.web.fashionshop.entity.Category;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

    // ID for update operations
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 50, message = "Tên danh mục phải từ 2-50 ký tự")
    private String categoryName;

    // Parent category - có thể null (root category)
    private Category parentCategory;

    @NotBlank(message = "Slug không được để trống")
    @Size(min = 2, max = 255, message = "Slug phải từ 2-255 ký tự")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    private String slug;

    @Size(max = 255, message = "URL ảnh tối đa 255 ký tự")
    private String imageUrl;

    private Boolean isActive = true;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime updatedAt;
}
