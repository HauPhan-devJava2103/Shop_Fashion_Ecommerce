package vn.web.fashionshop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.web.fashionshop.dto.cart.CartDto;
import vn.web.fashionshop.dto.cart.CartItemDto;
import vn.web.fashionshop.dto.cart.CartSummaryDto;
import vn.web.fashionshop.entity.Cart;
import vn.web.fashionshop.entity.CartItem;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.ProductVariant;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.repository.CartItemRepository;
import vn.web.fashionshop.repository.CartRepository;
import vn.web.fashionshop.repository.ProductRepository;
import vn.web.fashionshop.repository.ProductVariantRepository;
import vn.web.fashionshop.repository.UserRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public static String currentUserEmailOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User u) {
            return u.getUsername();
        }
        if (principal instanceof String s) {
            if ("anonymousUser".equalsIgnoreCase(s)) {
                return null;
            }
            return s;
        }

        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }
        return name;
    }

    @Transactional(readOnly = true)
    public CartDto getMyCart() {
        String email = currentUserEmailOrNull();
        if (email == null) {
            return new CartDto(List.of(), BigDecimal.ZERO, 0);
        }
        Cart cart = cartRepository.findByUserEmailWithItems(email).orElse(null);
        if (cart == null) {
            return new CartDto(List.of(), BigDecimal.ZERO, 0);
        }
        return toDto(cart);
    }

    @Transactional(readOnly = true)
    public CartSummaryDto getMyCartSummary() {
        CartDto dto = getMyCart();
        return new CartSummaryDto(dto.totalQuantity(), dto.subtotal());
    }

    @Transactional
    public CartDto addProductToMyCart(long productId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        String email = currentUserEmailOrNull();
        if (email == null) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("USER_NOT_FOUND"));

        Cart cart = cartRepository.findByUser_Email(email).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(c);
        });

        Product product = productRepository.findByIdWithVariants(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));

        ProductVariant variant = pickDefaultVariant(product);
        if (variant == null || variant.getId() == null) {
            variant = createDefaultVariant(product);
        }

        CartItem item = cartItemRepository.findByCart_IdAndVariant_Id(cart.getId(), variant.getId()).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setQuantity(quantity);
            item.setUnitPrice(computeDiscountedPrice(product));
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setUpdatedAt(LocalDateTime.now());
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Reload with items for DTO
        Cart refreshed = cartRepository.findByUserEmailWithItems(email).orElse(cart);
        return toDto(refreshed);
    }

    @Transactional
    public CartDto addVariantToMyCart(long variantId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        String email = currentUserEmailOrNull();
        if (email == null) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("USER_NOT_FOUND"));

        Cart cart = cartRepository.findByUser_Email(email).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(c);
        });

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("VARIANT_NOT_FOUND"));
        Product product = variant.getProduct();

        CartItem item = cartItemRepository.findByCart_IdAndVariant_Id(cart.getId(), variant.getId()).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setQuantity(quantity);
            item.setUnitPrice(computeDiscountedPrice(product));
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setUpdatedAt(LocalDateTime.now());
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        Cart refreshed = cartRepository.findByUserEmailWithItems(email).orElse(cart);
        return toDto(refreshed);
    }

    @Transactional
    public CartDto updateMyCartItemQuantity(long itemId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        String email = currentUserEmailOrNull();
        if (email == null) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }

        Cart cart = cartRepository.findByUserEmailWithItems(email)
                .orElseThrow(() -> new IllegalStateException("CART_NOT_FOUND"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId() != null && i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ITEM_NOT_FOUND"));

        item.setQuantity(quantity);
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        item.setUpdatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public CartDto removeMyCartItem(long itemId) {
        String email = currentUserEmailOrNull();
        if (email == null) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }

        Cart cart = cartRepository.findByUserEmailWithItems(email)
                .orElseThrow(() -> new IllegalStateException("CART_NOT_FOUND"));

        boolean removed = cart.getItems().removeIf(i -> i.getId() != null && i.getId().equals(itemId));
        if (!removed) {
            throw new IllegalArgumentException("ITEM_NOT_FOUND");
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        return toDto(cart);
    }

    private static ProductVariant pickDefaultVariant(Product product) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return null;
        }
        // Prefer in-stock variants, then lowest id
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

    private static CartDto toDto(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return new CartDto(List.of(), BigDecimal.ZERO, 0);
        }

        List<CartItemDto> items = cart.getItems().stream().map(i -> {
            ProductVariant v = i.getVariant();
            Product p = v != null ? v.getProduct() : null;
            String imageUrl = p != null ? p.getMainImageUrl() : "/images/no-image.png";
            return new CartItemDto(
                    i.getId(),
                    v != null ? v.getId() : null,
                    p != null ? p.getId() : null,
                    p != null ? p.getProductName() : "",
                    imageUrl,
                    v != null ? v.getColor() : null,
                    v != null ? v.getSize() : null,
                    i.getQuantity(),
                    i.getUnitPrice(),
                    i.getTotalPrice());
        }).toList();

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
}
