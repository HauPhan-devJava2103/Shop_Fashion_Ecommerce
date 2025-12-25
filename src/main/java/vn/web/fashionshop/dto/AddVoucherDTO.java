package vn.web.fashionshop.dto;

import java.math.BigDecimal;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddVoucherDTO {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 20, message = "Mã voucher tối đa 20 ký tự")
    private String code;

    @NotNull(message = "Phần trăm giảm giá không được để trống")
    @Min(value = 0, message = "Phần trăm giảm giá phải >= 0")
    @Max(value = 100, message = "Phần trăm giảm giá phải <= 100")
    private Integer discountPercent;

    @Min(value = 0, message = "Số tiền giảm tối đa phải >= 0")
    private BigDecimal maxDiscountAmount;

    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu phải >= 0")
    private BigDecimal minOrderValue;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @Min(value = 1, message = "Giới hạn sử dụng phải ít nhất là 1")
    private Integer usageLimit;

    private Boolean isActive = true;

    // Thời gian bắt đầu hiệu lực
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    // Thời gian hết hạn
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endAt;

}
