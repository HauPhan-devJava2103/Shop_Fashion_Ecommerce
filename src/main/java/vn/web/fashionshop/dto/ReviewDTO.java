package vn.web.fashionshop.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.web.fashionshop.entity.OrderItem;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {

    private Long id;

    private OrderItem orderItem;

    @NotNull(message = "Sản phẩm không được để trống")
    private Product product;

    @NotNull(message = "Người dùng không được để trống")
    private User user;

    @NotNull(message = "Đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa là 5 sao")
    private Integer rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(min = 10, max = 500, message = "Nội dung đánh giá phải từ 10-500 ký tự")
    private String comment;

    @Size(max = 255, message = "URL ảnh tối đa 255 ký tự")
    private String imageUrl;

    private Boolean isApproved = false;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime updatedAt;
}
