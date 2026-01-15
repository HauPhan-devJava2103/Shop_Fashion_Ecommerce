package vn.web.fashionshop.dto.cart;

import java.math.BigDecimal;

public record CartSummaryDto(
        Integer totalQuantity,
        BigDecimal subtotal) {
}
