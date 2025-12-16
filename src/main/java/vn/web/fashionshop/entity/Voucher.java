package vn.web.fashionshop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vouchers", indexes = {
        @Index(name = "idx_voucher_code", columnList = "code", unique = true),
        @Index(name = "idx_voucher_active", columnList = "is_active"),
        @Index(name = "idx_voucher_dates", columnList = "start_at,end_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // % giảm: 0 - 100
    @Min(0)
    @Max(100)
    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    // Giảm tối đa bao nhiêu tiền (VD: giảm 50% nhưng tối đa 100k)
    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    // Giá trị đơn hàng tối thiểu để sử dụng voucher
    @Column(name = "min_order_value", precision = 15, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    // Tổng số lần dùng tối đa (null = unlimited)
    @Column(name = "usage_limit")
    private Integer usageLimit;

    // Đã dùng bao nhiêu lần
    @Min(0)
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method: Kiểm tra voucher còn hiệu lực không
    public boolean isValid() {
        if (!isActive)
            return false;

        LocalDateTime now = LocalDateTime.now();
        if (startAt != null && now.isBefore(startAt))
            return false;
        if (endAt != null && now.isAfter(endAt))
            return false;

        if (usageLimit != null && usedCount >= usageLimit)
            return false;

        return true;
    }

    // Helper method: Kiểm tra đơn hàng có đủ điều kiện dùng voucher không
    public boolean isApplicable(BigDecimal orderValue) {
        if (!isValid())
            return false;
        if (minOrderValue != null && orderValue.compareTo(minOrderValue) < 0)
            return false;
        return true;
    }

    // Helper method: Tính số tiền giảm
    public BigDecimal calculateDiscount(BigDecimal orderValue) {
        BigDecimal discount = orderValue.multiply(new BigDecimal(discountPercent))
                .divide(new BigDecimal(100));

        // Áp dụng max discount nếu có
        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            return maxDiscountAmount;
        }

        return discount;
    }
}
