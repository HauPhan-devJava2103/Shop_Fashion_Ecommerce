package vn.web.fashionshop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

import vn.web.fashionshop.dto.cart.CartDto;
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.CheckoutService;
import vn.web.fashionshop.service.GuestCartService;
import vn.web.fashionshop.util.GuestCartCookieUtil;

@Controller
public class CartController {

    private static final String SESSION_VOUCHER_CODE = "CART_VOUCHER_CODE";

    private final CartService cartService;
    private final GuestCartService guestCartService;
    private final CheckoutService checkoutService;

    public CartController(CartService cartService, GuestCartService guestCartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.guestCartService = guestCartService;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/cart")
    public String cartPage(HttpServletRequest request, Model model) {
        CartDto cart = shouldUseGuestCart(request)
                ? guestCartService.getGuestCart(request)
                : (isAuthenticated() ? cartService.getMyCart() : guestCartService.getGuestCart(request));
        model.addAttribute("cart", cart);

        // Voucher preview (stored in session)
        HttpSession session = request != null ? request.getSession(false) : null;
        String voucherCode = session != null ? (String) session.getAttribute(SESSION_VOUCHER_CODE) : null;
        if (voucherCode != null && cart != null) {
            try {
                CheckoutService.VoucherPreview preview = checkoutService.previewVoucher(voucherCode, cart.subtotal());
                model.addAttribute("voucherPreview", preview);
            } catch (Exception ex) {
                // If voucher becomes invalid (expired/min order), clear it
                if (session != null) {
                    session.removeAttribute(SESSION_VOUCHER_CODE);
                }
                model.addAttribute("voucherError", ex.getMessage());
            }
        }

        return "cart";
    }

    @PostMapping("/cart/voucher/apply")
    public String applyVoucher(
            @RequestParam("code") String code,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String normalized = normalizeVoucherCode(code);
        if (normalized == null) {
            redirectAttributes.addFlashAttribute("voucherError", "Vui lòng nhập mã giảm giá");
            return "redirect:/cart";
        }

        CartDto cart = shouldUseGuestCart(request)
                ? guestCartService.getGuestCart(request)
                : (isAuthenticated() ? cartService.getMyCart() : guestCartService.getGuestCart(request));

        try {
            checkoutService.previewVoucher(normalized, cart != null ? cart.subtotal() : null);
            request.getSession(true).setAttribute(SESSION_VOUCHER_CODE, normalized);
            redirectAttributes.addFlashAttribute("voucherSuccess", "Đã áp dụng mã " + normalized);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("voucherError", ex.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/voucher/clear")
    public String clearVoucher(HttpServletRequest request) {
        HttpSession session = request != null ? request.getSession(false) : null;
        if (session != null) {
            session.removeAttribute(SESSION_VOUCHER_CODE);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/add/{productId}")
    public String addToCart(
            @PathVariable("productId") long productId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            HttpServletRequest request,
            HttpServletResponse response) {

        int qty = (quantity == null || quantity <= 0) ? 1 : quantity;

        if (isAuthenticated() && !shouldUseGuestCart(request)) {
            try {
                cartService.addProductToMyCart(productId, qty);
            } catch (Exception ex) {
                // If DB cart fails (constraint/migration issues), fall back to guest cookie cart.
                guestCartService.addProduct(request, response, productId, qty);
            }
        } else {
            guestCartService.addProduct(request, response, productId, qty);
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{id}/quantity")
    public String updateQuantity(
            @PathVariable("id") long id,
            @RequestParam("quantity") int quantity,
            HttpServletRequest request,
            HttpServletResponse response) {

        int qty = quantity <= 0 ? 1 : quantity;
        if (isAuthenticated() && !shouldUseGuestCart(request)) {
            cartService.updateMyCartItemQuantity(id, qty);
        } else {
            guestCartService.updateVariantQuantity(request, response, id, qty);
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{id}/remove")
    public String removeItem(
            @PathVariable("id") long id,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (isAuthenticated() && !shouldUseGuestCart(request)) {
            cartService.removeMyCartItem(id);
        } else {
            guestCartService.removeVariant(request, response, id);
        }
        return "redirect:/cart";
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

    private static String normalizeVoucherCode(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t.toUpperCase(Locale.ROOT);
    }
}
