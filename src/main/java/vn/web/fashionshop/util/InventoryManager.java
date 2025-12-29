package vn.web.fashionshop.util;

import java.time.LocalDateTime;

import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.ProductVariant;

// Xử lý restock product and product variant
public class InventoryManager {

    /**
     * Reserve stock (trừ stock khi tạo order hoặc tăng quantity)
     * Validates and decreases stock for both Product and ProductVariant
     * 
     * @param variant  ProductVariant to reserve stock from
     * @param quantity Amount of stock to reserve
     * @throws IllegalArgumentException if insufficient stock
     */
    public static void reserveStock(ProductVariant variant, Integer quantity) {
        if (quantity <= 0) {
            return; // Nothing to reserve
        }

        Product product = variant.getProduct();

        // Validate variant stock
        if (variant.getStock() < quantity) {
            throw new IllegalArgumentException(
                    String.format("Không đủ hàng trong kho (Variant: %s). Còn lại: %d, Cần: %d",
                            variant.getSkuVariant(), variant.getStock(), quantity));
        }

        // Validate product stock
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException(
                    String.format("Không đủ hàng trong kho (Product: %s). Còn lại: %d, Cần: %d",
                            product.getProductName(), product.getStock(), quantity));
        }

        // Reserve stock (decrease)
        variant.setStock(variant.getStock() - quantity);
        product.setStock(product.getStock() - quantity);

        // Update timestamps
        variant.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Release stock (hoàn trả stock khi cancel order hoặc giảm quantity)
     * Increases stock for both Product and ProductVariant
     * 
     * @param variant  ProductVariant to release stock to
     * @param quantity Amount of stock to release
     */
    public static void releaseStock(ProductVariant variant, Integer quantity) {
        if (quantity <= 0) {
            return; // Nothing to release
        }

        Product product = variant.getProduct();

        // Release stock (increase)
        variant.setStock(variant.getStock() + quantity);
        product.setStock(product.getStock() + quantity);

        // Update timestamps
        variant.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Adjust stock based on quantity delta
     * Automatically reserves or releases stock based on positive/negative delta
     * 
     * @param variant       ProductVariant to adjust
     * @param quantityDelta Positive = reserve (decrease stock), Negative = release
     *                      (increase stock)
     * @throws IllegalArgumentException if insufficient stock for reservation
     */
    public static void adjustStock(ProductVariant variant, Integer quantityDelta) {
        if (quantityDelta > 0) {
            // Positive delta: Tăng quantity → Reserve (trừ stock)
            reserveStock(variant, quantityDelta);
        } else if (quantityDelta < 0) {
            // Negative delta: Giảm quantity → Release (hoàn trả stock)
            releaseStock(variant, Math.abs(quantityDelta));
        }
        // quantityDelta == 0 → No change needed
    }

    /**
     * Check if variant has sufficient stock
     * 
     * @param variant  ProductVariant to check
     * @param quantity Required quantity
     * @return true if both variant and product have sufficient stock
     */
    public static boolean hasStock(ProductVariant variant, Integer quantity) {
        if (variant == null || quantity <= 0) {
            return false;
        }

        Product product = variant.getProduct();
        return variant.getStock() >= quantity && product.getStock() >= quantity;
    }

    /**
     * Get available stock for variant (minimum of variant and product stock)
     * 
     * @param variant ProductVariant to check
     * @return Available stock quantity
     */
    public static Integer getAvailableStock(ProductVariant variant) {
        if (variant == null || variant.getProduct() == null) {
            return 0;
        }

        return Math.min(variant.getStock(), variant.getProduct().getStock());
    }
}
