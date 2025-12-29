package vn.web.fashionshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO for Order AJAX responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String userFullName;
    private String userEmail;
}
