package vn.web.fashionshop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import vn.web.fashionshop.entity.OrderItem;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.Voucher;

/**
 * Utility class for Order price calculations
 */
public class OrderCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Tính giá sau khi áp dụng discount của Product
     * Formula: price - (price × discount / 100)
     * 
     * @param product Product entity
     * @return Unit price sau discount
     */
    public static BigDecimal calculateUnitPriceFromProduct(Product product) {
        if (product == null || product.getPrice() == null) {
            throw new IllegalArgumentException("Product or price cannot be null");
        }

        BigDecimal price = product.getPrice();
        BigDecimal discount = product.getDiscount();

        // Nếu không có discount hoặc discount = 0
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
            return price.setScale(SCALE, ROUNDING_MODE);
        }

        // Tính số tiền giảm: price × discount / 100
        BigDecimal discountAmount = price.multiply(discount)
                .divide(new BigDecimal("100"), SCALE, ROUNDING_MODE);

        // Giá sau giảm
        return price.subtract(discountAmount).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Tính total price cho 1 OrderItem
     * Formula: unit_price × quantity
     * 
     * @param unitPrice Đơn giá
     * @param quantity  Số lượng
     * @return Tổng tiền
     */
    public static BigDecimal calculateOrderItemTotal(BigDecimal unitPrice, Integer quantity) {
        if (unitPrice == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Unit price and quantity must be valid");
        }

        return unitPrice.multiply(new BigDecimal(quantity))
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Tính tổng tiền hàng (chưa trừ voucher)
     * Formula: SUM(order_item.total_price)
     * 
     * @param orderItems Danh sách OrderItem
     * @return Sub total
     */
    public static BigDecimal calculateSubTotal(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }

        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Tính số tiền giảm từ voucher
     * Formula: sub_total × voucher_discount / 100
     * Áp dụng max_discount_amount nếu có
     * 
     * @param subTotal Tổng tiền hàng
     * @param voucher  Voucher entity (có thể null)
     * @return Discount amount
     */
    public static BigDecimal calculateVoucherDiscount(BigDecimal subTotal, Voucher voucher) {
        if (subTotal == null) {
            throw new IllegalArgumentException("Sub total cannot be null");
        }

        // Không có voucher
        if (voucher == null || voucher.getDiscountPercent() == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }

        // Kiểm tra min order value
        if (voucher.getMinOrderValue() != null
                && subTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }

        // Tính discount amount
        BigDecimal discountPercent = new BigDecimal(voucher.getDiscountPercent());
        BigDecimal discountAmount = subTotal.multiply(discountPercent)
                .divide(new BigDecimal("100"), SCALE, ROUNDING_MODE);

        // Áp dụng max discount amount nếu có
        if (voucher.getMaxDiscountAmount() != null
                && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            return voucher.getMaxDiscountAmount().setScale(SCALE, ROUNDING_MODE);
        }

        return discountAmount.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Tính tổng tiền cuối cùng phải trả
     * Formula: sub_total - discount_amount
     * 
     * @param subTotal       Tổng tiền hàng
     * @param discountAmount Số tiền giảm
     * @return Total amount
     */
    public static BigDecimal calculateTotalAmount(BigDecimal subTotal, BigDecimal discountAmount) {
        if (subTotal == null || discountAmount == null) {
            throw new IllegalArgumentException("Sub total and discount amount cannot be null");
        }

        BigDecimal total = subTotal.subtract(discountAmount);

        // Đảm bảo total không âm
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }

        return total.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Validate voucher có thể sử dụng được không
     * 
     * @param voucher  Voucher entity
     * @param subTotal Sub total của order
     * @throws IllegalArgumentException nếu voucher không hợp lệ
     */
    public static void validateVoucher(Voucher voucher, BigDecimal subTotal) {
        if (voucher == null) {
            return; // Không có voucher là hợp lệ
        }

        // Check active
        if (!voucher.getIsActive()) {
            throw new IllegalArgumentException("Voucher is not active");
        }

        // Check min order value
        if (voucher.getMinOrderValue() != null
                && subTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException(
                    String.format("Order must be at least %s to use this voucher",
                            voucher.getMinOrderValue()));
        }

        // Check usage limit
        if (voucher.getUsageLimit() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Voucher usage limit exceeded");
        }
    }
}
