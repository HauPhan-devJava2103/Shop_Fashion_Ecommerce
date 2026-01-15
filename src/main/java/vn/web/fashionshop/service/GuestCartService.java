package vn.web.fashionshop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.web.fashionshop.dto.cart.CartDto;
import vn.web.fashionshop.dto.cart.CartItemDto;
import vn.web.fashionshop.dto.cart.CartSummaryDto;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.ProductVariant;
import vn.web.fashionshop.repository.ProductRepository;
import vn.web.fashionshop.repository.ProductVariantRepository;
import vn.web.fashionshop.util.GuestCartCookieUtil;

@Service
public class GuestCartService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public GuestCartService(ProductRepository productRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public CartDto getGuestCart(HttpServletRequest request) {
        Map<Long, Integer> raw = GuestCartCookieUtil.readGuestCart(request != null ? request.getCookies() : null);
        if (raw.isEmpty()) {
            return new CartDto(List.of(), BigDecimal.ZERO, 0);
        }

        List<Long> ids = new ArrayList<>(raw.keySet());
        List<ProductVariant> variants = productVariantRepository.findByIdInWithProductAndImages(ids);
        Map<Long, ProductVariant> variantMap = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            if (v != null && v.getId() != null) {
                variantMap.put(v.getId(), v);
            }
        }

        List<CartItemDto> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : raw.entrySet()) {
            Long variantId = e.getKey();
            Integer qty = e.getValue();
            if (variantId == null || qty == null || qty <= 0) {
                continue;
            }
            ProductVariant v = variantMap.get(variantId);
            if (v == null) {
                continue;
            }
            Product p = v.getProduct();
            BigDecimal unit = computeDiscountedPrice(p);
            BigDecimal total = unit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
            items.add(new CartItemDto(
                    // For guest we reuse variantId as stable identifier
                    variantId,
                    variantId,
                    p != null ? p.getId() : null,
                    p != null ? p.getProductName() : "",
                    p != null ? p.getMainImageUrl() : "/images/no-image.png",
                    v.getColor(),
                    v.getSize(),
                    qty,
                    unit,
                    total));
        }

        items.sort(Comparator.comparing(CartItemDto::id));

        BigDecimal subtotal = items.stream()
                .map(CartItemDto::totalPrice)
                .filter(x -> x != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQty = items.stream()
                .map(CartItemDto::quantity)
                .filter(q -> q != null)
                .reduce(0, Integer::sum);

        return new CartDto(items, subtotal, totalQty);
    }

    public CartSummaryDto getGuestCartSummary(HttpServletRequest request) {
        CartDto dto = getGuestCart(request);
        return new CartSummaryDto(dto.totalQuantity(), dto.subtotal());
    }

    @Transactional
    public void addProduct(HttpServletRequest request, HttpServletResponse response, long productId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        Product product = productRepository.findByIdWithVariants(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));

        ProductVariant variant = pickDefaultVariant(product);
        if (variant == null || variant.getId() == null) {
            variant = createDefaultVariant(product);
        }

        Map<Long, Integer> cart = GuestCartCookieUtil.readGuestCart(request != null ? request.getCookies() : null);
        int current = cart.getOrDefault(variant.getId(), 0);
        cart.put(variant.getId(), current + quantity);

        Cookie cookie = GuestCartCookieUtil.buildCookie(cart);
        response.addCookie(cookie);
    }

    public void updateVariantQuantity(HttpServletRequest request, HttpServletResponse response, long variantId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }
        Map<Long, Integer> cart = GuestCartCookieUtil.readGuestCart(request != null ? request.getCookies() : null);
        if (!cart.containsKey(variantId)) {
            return;
        }
        cart.put(variantId, quantity);
        response.addCookie(GuestCartCookieUtil.buildCookie(cart));
    }

    @Transactional
    public void addVariant(HttpServletRequest request, HttpServletResponse response, long variantId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }
        // Validate variant exists
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("VARIANT_NOT_FOUND"));
        if (variant.getId() == null) {
            throw new IllegalArgumentException("VARIANT_NOT_FOUND");
        }

        Map<Long, Integer> cart = GuestCartCookieUtil.readGuestCart(request != null ? request.getCookies() : null);
        int current = cart.getOrDefault(variant.getId(), 0);
        cart.put(variant.getId(), current + quantity);
        response.addCookie(GuestCartCookieUtil.buildCookie(cart));
    }

    public void removeVariant(HttpServletRequest request, HttpServletResponse response, long variantId) {
        Map<Long, Integer> cart = GuestCartCookieUtil.readGuestCart(request != null ? request.getCookies() : null);
        cart.remove(variantId);
        if (cart.isEmpty()) {
            response.addCookie(GuestCartCookieUtil.clearCookie());
        } else {
            response.addCookie(GuestCartCookieUtil.buildCookie(cart));
        }
    }

    private static ProductVariant pickDefaultVariant(Product product) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return null;
        }
        return product.getVariants().stream()
                .sorted(Comparator
                        .comparing((ProductVariant v) -> v.getStock() != null && v.getStock() > 0 ? 0 : 1)
                        .thenComparing(v -> v.getId() != null ? v.getId() : Long.MAX_VALUE))
                .findFirst()
                .orElse(null);
    }

    private ProductVariant createDefaultVariant(Product product) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
        }

        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setSize(null);
        v.setColor(null);
        v.setStock(product.getStock() != null ? product.getStock() : 0);
        v.setSkuVariant(product.getSku() + "-DEFAULT");
        v.setCreatedAt(LocalDateTime.now());
        return productVariantRepository.save(v);
    }

    private static BigDecimal computeDiscountedPrice(Product product) {
        if (product == null || product.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = product.getPrice();
        BigDecimal discount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }
        return price.subtract(price.multiply(discount).divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
