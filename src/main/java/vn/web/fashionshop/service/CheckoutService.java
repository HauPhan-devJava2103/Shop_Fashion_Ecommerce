package vn.web.fashionshop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.web.fashionshop.dto.checkout.CheckoutForm;
import vn.web.fashionshop.entity.Cart;
import vn.web.fashionshop.entity.CartItem;
import vn.web.fashionshop.entity.Order;
import vn.web.fashionshop.entity.OrderAddress;
import vn.web.fashionshop.entity.OrderItem;
import vn.web.fashionshop.entity.Payment;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.ProductVariant;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.entity.Voucher;
import vn.web.fashionshop.enums.EOrderStatus;
import vn.web.fashionshop.enums.EPaymentStatus;
import vn.web.fashionshop.repository.CartItemRepository;
import vn.web.fashionshop.repository.CartRepository;
import vn.web.fashionshop.repository.OrderRepository;
import vn.web.fashionshop.repository.ProductRepository;
import vn.web.fashionshop.repository.ProductVariantRepository;
import vn.web.fashionshop.repository.UserRepository;
import vn.web.fashionshop.repository.VoucherRepository;
import vn.web.fashionshop.util.GuestCartCookieUtil;

@Service
public class CheckoutService {

    public record VoucherPreview(
            String voucherCode,
            Integer voucherDiscountPercent,
            BigDecimal discountAmount,
            BigDecimal totalAmount) {
    }

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;

    public CheckoutService(
            UserRepository userRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductVariantRepository productVariantRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            VoucherRepository voucherRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.voucherRepository = voucherRepository;
    }

    @Transactional(readOnly = true)
    public CheckoutForm buildPrefilledForm(String email) {
        CheckoutForm form = new CheckoutForm();
        if (email == null || email.isBlank()) {
            return form;
        }
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return form;
        }
        User user = userOpt.get();
        form.setRecipientName(user.getFullName());
        form.setPhone(user.getPhone());
        form.setAddressLine(extractStreetAddressLine(user.getAddress()));
        return form;
    }

    @Transactional(readOnly = true)
    public VoucherPreview previewVoucher(String voucherCodeRaw, BigDecimal subtotal) {
        String voucherCode = trimToNull(voucherCodeRaw);
        if (voucherCode == null) {
            return new VoucherPreview(null, null, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    (subtotal != null ? subtotal : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal base = (subtotal != null ? subtotal : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã voucher không tồn tại"));

        if (!voucher.isApplicable(base)) {
            throw new IllegalArgumentException("Voucher không hợp lệ hoặc không đủ điều kiện áp dụng");
        }

        BigDecimal discountAmount = voucher.calculateDiscount(base).setScale(2, RoundingMode.HALF_UP);
        if (discountAmount.compareTo(base) > 0) {
            discountAmount = base;
        }
        BigDecimal totalAmount = base.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        return new VoucherPreview(voucher.getCode(), voucher.getDiscountPercent(), discountAmount, totalAmount);
    }

    @Transactional
    public Long placeOrder(String email, CheckoutForm form, HttpServletRequest request, HttpServletResponse response) {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("USER_NOT_FOUND"));

        // If guest cart exists, merge into DB cart first so checkout uses a single
        // source.
        Map<Long, Integer> guest = GuestCartCookieUtil.readGuestCart(request != null ? request.getCookies() : null);
        if (!guest.isEmpty()) {
            mergeGuestCartIntoUserCart(user, guest);
            if (response != null) {
                response.addCookie(GuestCartCookieUtil.clearCookie());
            }
        }

        Cart cart = cartRepository.findByUserEmailWithItems(email)
                .orElseThrow(() -> new IllegalStateException("CART_NOT_FOUND"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("CART_EMPTY");
        }

        LocalDateTime now = LocalDateTime.now();

        Order order = new Order();
        order.setUser(user);
        order.setSubTotal(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPaymentMethod(form.getPaymentMethod());
        order.setOrderStatus(EOrderStatus.PENDING);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // Collect variants and products that need stock update
        List<ProductVariant> variantsToUpdate = new ArrayList<>();
        List<Product> productsToUpdate = new ArrayList<>();

        for (CartItem ci : cart.getItems()) {
            if (ci == null || ci.getVariant() == null || ci.getVariant().getId() == null) {
                continue;
            }

            ProductVariant variant = ci.getVariant();
            Product product = variant.getProduct();
            int qty = ci.getQuantity() != null && ci.getQuantity() > 0 ? ci.getQuantity() : 1;

            // Kiểm tra tồn kho variant
            int variantStock = variant.getStock() != null ? variant.getStock() : 0;
            if (variantStock < qty) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getProductName()
                        + "' (Size: " + variant.getSize() + ", Màu: " + variant.getColor()
                        + ") không đủ số lượng. Còn lại: " + variantStock);
            }

            // Kiểm tra tồn kho product
            int productStock = product.getStock() != null ? product.getStock() : 0;
            if (productStock < qty) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getProductName()
                        + "' không đủ số lượng trong kho. Còn lại: " + productStock);
            }

            BigDecimal unit = ci.getUnitPrice() != null ? ci.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setVariant(variant);
            oi.setQuantity(qty);
            oi.setUnitPrice(unit.setScale(2, RoundingMode.HALF_UP));
            oi.setTotalPrice(lineTotal);
            oi.setCreatedAt(now);
            oi.setUpdatedAt(now);
            orderItems.add(oi);
            subtotal = subtotal.add(lineTotal);

            // Trừ stock variant
            variant.setStock(variantStock - qty);
            variant.setUpdatedAt(now);
            variantsToUpdate.add(variant);

            // Trừ stock product
            product.setStock(productStock - qty);
            product.setUpdatedAt(now);
            productsToUpdate.add(product);
        }

        if (orderItems.isEmpty()) {
            throw new IllegalStateException("CART_EMPTY");
        }

        // Lưu các variant và product đã trừ stock
        productVariantRepository.saveAll(variantsToUpdate);
        productRepository.saveAll(productsToUpdate);

        order.setOrderItems(orderItems);
        order.setSubTotal(subtotal.setScale(2, RoundingMode.HALF_UP));

        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        String voucherCode = trimToNull(form.getVoucherCode());
        if (voucherCode != null) {
            voucher = voucherRepository.findByCode(voucherCode)
                    .orElseThrow(() -> new IllegalArgumentException("Mã voucher không tồn tại"));
            if (!voucher.isApplicable(order.getSubTotal())) {
                throw new IllegalArgumentException("Voucher không hợp lệ hoặc không đủ điều kiện áp dụng");
            }
            discountAmount = voucher.calculateDiscount(order.getSubTotal()).setScale(2, RoundingMode.HALF_UP);
            if (discountAmount.compareTo(order.getSubTotal()) > 0) {
                discountAmount = order.getSubTotal();
            }
            order.setVoucher(voucher);
            order.setVoucherCode(voucher.getCode());
            order.setVoucherDiscountPercent(voucher.getDiscountPercent());
        }

        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(order.getSubTotal().subtract(order.getDiscountAmount()).setScale(2, RoundingMode.HALF_UP));

        OrderAddress address = new OrderAddress();
        address.setOrder(order);
        address.setRecipientName(trimToNull(form.getRecipientName()));
        address.setPhone(trimToNull(form.getPhone()));
        // Checkout address is sourced from the user's profile (DB) and only stores "street" part.
        address.setAddressLine(extractStreetAddressLine(user.getAddress()));
        address.setNote(trimToNull(form.getNote()));
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
        order.setOrderAddress(address);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(form.getPaymentMethod());
        payment.setStatus(EPaymentStatus.PENDING);
        payment.setAmount(order.getTotalAmount());
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        order.setPayment(payment);

        Order saved = orderRepository.save(order);

        if (voucher != null) {
            Integer used = voucher.getUsedCount() != null ? voucher.getUsedCount() : 0;
            voucher.setUsedCount(used + 1);
            voucher.setUpdatedAt(now);
            voucherRepository.save(voucher);
        }

        // Clear DB cart after successful order creation.
        cart.getItems().clear();
        cart.setUpdatedAt(now);
        cartRepository.save(cart);

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public Order getMyOrderForSuccessPage(String email, Long orderId) {
        if (email == null || email.isBlank() || orderId == null) {
            return null;
        }
        // Minimal fetch: order + address + items + variant + product (no
        // product.images)
        return orderRepository.findByIdAndUserEmailWithDetails(orderId, email).orElse(null);
    }

    private void mergeGuestCartIntoUserCart(User user, Map<Long, Integer> guest) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank() || guest == null || guest.isEmpty()) {
            return;
        }

        String email = user.getEmail();
        Cart cart = cartRepository.findByUser_Email(email).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(c);
        });

        Map<Long, Integer> normalized = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> e : guest.entrySet()) {
            Long variantId = e.getKey();
            Integer qty = e.getValue();
            if (variantId == null || qty == null || qty <= 0) {
                continue;
            }
            normalized.put(variantId, qty);
        }
        if (normalized.isEmpty()) {
            return;
        }

        List<ProductVariant> variants = productVariantRepository.findAllById(new ArrayList<>(normalized.keySet()));
        Map<Long, ProductVariant> variantMap = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            if (v != null && v.getId() != null) {
                variantMap.put(v.getId(), v);
            }
        }

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Long, Integer> e : normalized.entrySet()) {
            Long variantId = e.getKey();
            Integer qty = e.getValue();
            ProductVariant variant = variantMap.get(variantId);
            if (variant == null) {
                continue;
            }

            CartItem item = cartItemRepository.findByCart_IdAndVariant_Id(cart.getId(), variantId).orElse(null);
            if (item == null) {
                item = new CartItem();
                item.setCart(cart);
                item.setVariant(variant);
                item.setQuantity(qty);
                item.setUnitPrice(computeDiscountedPrice(variant.getProduct()));
                item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                item.setCreatedAt(now);
                item.setUpdatedAt(now);
                cart.getItems().add(item);
            } else {
                int newQty = (item.getQuantity() != null ? item.getQuantity() : 0) + qty;
                if (newQty <= 0) {
                    newQty = qty;
                }
                item.setQuantity(newQty);
                if (item.getUnitPrice() == null) {
                    item.setUnitPrice(computeDiscountedPrice(variant.getProduct()));
                }
                item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                item.setUpdatedAt(now);
            }
        }

        cart.setUpdatedAt(now);
        cartRepository.save(cart);
    }

    /**
     * Extract only "house number + street name" from a saved address.
     * Example: "123 Nguyễn Huệ, Q1, TP.HCM" -> "123 Nguyễn Huệ".
     */
    private static String extractStreetAddressLine(String rawAddress) {
        String a = trimToNull(rawAddress);
        if (a == null) {
            return null;
        }
        int comma = a.indexOf(',');
        if (comma >= 0) {
            a = a.substring(0, comma);
        }
        return trimToNull(a);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private static BigDecimal computeDiscountedPrice(Product product) {
        if (product == null || product.getPrice() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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
