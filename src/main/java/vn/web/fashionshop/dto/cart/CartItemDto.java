package vn.web.fashionshop.dto.cart;

import java.math.BigDecimal;

import vn.web.fashionshop.enums.ESize;

public record CartItemDto(
        Long id,
        Long variantId,
        Long productId,
        String productName,
        String imageUrl,
        String color,
        ESize size,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice) {
}
