package vn.web.fashionshop.dto.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        List<CartItemDto> items,
        BigDecimal subtotal,
        Integer totalQuantity) {
}
