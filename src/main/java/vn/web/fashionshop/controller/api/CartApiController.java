package vn.web.fashionshop.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.web.fashionshop.dto.cart.AddProductToCartRequest;
import vn.web.fashionshop.dto.cart.AddVariantToCartRequest;
import vn.web.fashionshop.dto.cart.CartDto;
import vn.web.fashionshop.dto.cart.CartSummaryDto;
import vn.web.fashionshop.dto.cart.UpdateCartItemQuantityRequest;
import vn.web.fashionshop.service.CheckoutService;
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.GuestCartService;
import vn.web.fashionshop.util.GuestCartCookieUtil;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private static final String SESSION_VOUCHER_CODE = "CART_VOUCHER_CODE";

    private final CartService cartService;
    private final GuestCartService guestCartService;
    private final CheckoutService checkoutService;

    public CartApiController(CartService cartService, GuestCartService guestCartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.guestCartService = guestCartService;
        this.checkoutService = checkoutService;
    }

    public record VoucherPreviewResponse(
            String voucherCode,
            Integer voucherDiscountPercent,
            java.math.BigDecimal discountAmount,
            java.math.BigDecimal totalAmount) {
    }

    public record CartPageStateResponse(
            CartDto cart,
            VoucherPreviewResponse voucherPreview,
            String voucherError) {
    }

    @GetMapping
    public CartDto getCart(HttpServletRequest request) {
        if (shouldUseGuestCart(request)) {
            return guestCartService.getGuestCart(request);
        }
        return isAuthenticated() ? cartService.getMyCart() : guestCartService.getGuestCart(request);
    }

    @GetMapping("/summary")
    public CartSummaryDto summary(HttpServletRequest request) {
        if (shouldUseGuestCart(request)) {
            return guestCartService.getGuestCartSummary(request);
        }
        return isAuthenticated() ? cartService.getMyCartSummary() : guestCartService.getGuestCartSummary(request);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> add(
            @RequestBody AddProductToCartRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {

        long productId = body != null && body.productId() != null ? body.productId() : 0L;
        int qty = body != null && body.quantity() != null ? body.quantity() : 1;
        if (productId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        if (isAuthenticated() && !shouldUseGuestCart(request)) {
            try {
                return ResponseEntity.ok(cartService.addProductToMyCart(productId, qty));
            } catch (Exception ex) {
                guestCartService.addProduct(request, response, productId, qty);
                return ResponseEntity.ok(guestCartService.getGuestCart(request));
            }
        }

        guestCartService.addProduct(request, response, productId, qty);
        return ResponseEntity.ok(guestCartService.getGuestCart(request));
    }

    @PostMapping("/add-variant")
    public ResponseEntity<CartDto> addVariant(
            @RequestBody AddVariantToCartRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {

        long variantId = body != null && body.variantId() != null ? body.variantId() : 0L;
        int qty = body != null && body.quantity() != null ? body.quantity() : 1;
        if (variantId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        if (qty <= 0) {
            qty = 1;
        }

        if (isAuthenticated() && !shouldUseGuestCart(request)) {
            try {
                return ResponseEntity.ok(cartService.addVariantToMyCart(variantId, qty));
            } catch (Exception ex) {
                guestCartService.addVariant(request, response, variantId, qty);
                return ResponseEntity.ok(guestCartService.getGuestCart(request));
            }
        }

        guestCartService.addVariant(request, response, variantId, qty);
        return ResponseEntity.ok(guestCartService.getGuestCart(request));
    }

    @PostMapping("/items/{id}/quantity")
    public ResponseEntity<CartDto> updateQuantity(
            @PathVariable("id") long id,
            @RequestBody UpdateCartItemQuantityRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {

        int qty = body != null && body.quantity() != null ? body.quantity() : 1;
        if (qty <= 0) {
            qty = 1;
        }

        if (isAuthenticated() && !shouldUseGuestCart(request)) {
            return ResponseEntity.ok(cartService.updateMyCartItemQuantity(id, qty));
        }

        guestCartService.updateVariantQuantity(request, response, id, qty);
        return ResponseEntity.ok(guestCartService.getGuestCart(request));
    }

    @PostMapping("/items/{id}/quantity/state")
    public ResponseEntity<CartPageStateResponse> updateQuantityState(
            @PathVariable("id") long id,
            @RequestBody UpdateCartItemQuantityRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {

        int qty = body != null && body.quantity() != null ? body.quantity() : 1;
        if (qty <= 0) {
            qty = 1;
        }

        CartDto cart;
        if (isAuthenticated() && !shouldUseGuestCart(request)) {
            cart = cartService.updateMyCartItemQuantity(id, qty);
        } else {
            guestCartService.updateVariantQuantity(request, response, id, qty);
            cart = guestCartService.getGuestCart(request);
        }

        return ResponseEntity.ok(buildPageState(cart, request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartDto> remove(
            @PathVariable("id") long id,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (isAuthenticated() && !shouldUseGuestCart(request)) {
            return ResponseEntity.ok(cartService.removeMyCartItem(id));
        }

        guestCartService.removeVariant(request, response, id);
        return ResponseEntity.ok(guestCartService.getGuestCart(request));
    }

    private static boolean isAuthenticated() {
        String email = CartService.currentUserEmailOrNull();
        return email != null && !email.isBlank();
    }

    private static boolean shouldUseGuestCart(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return !GuestCartCookieUtil.readGuestCart(request.getCookies()).isEmpty();
    }

    private CartPageStateResponse buildPageState(CartDto cart, HttpServletRequest request) {
        String voucherCode = request != null && request.getSession(false) != null
                ? (String) request.getSession(false).getAttribute(SESSION_VOUCHER_CODE)
                : null;

        if (voucherCode == null || voucherCode.isBlank()) {
            return new CartPageStateResponse(cart, null, null);
        }

        try {
            CheckoutService.VoucherPreview preview = checkoutService.previewVoucher(voucherCode,
                    cart != null ? cart.subtotal() : null);
            VoucherPreviewResponse dto = preview != null
                    ? new VoucherPreviewResponse(preview.voucherCode(), preview.voucherDiscountPercent(), preview.discountAmount(),
                            preview.totalAmount())
                    : null;
            return new CartPageStateResponse(cart, dto, null);
        } catch (Exception ex) {
            if (request != null && request.getSession(false) != null) {
                request.getSession(false).removeAttribute(SESSION_VOUCHER_CODE);
            }
            return new CartPageStateResponse(cart, null, ex.getMessage());
        }
    }
}
