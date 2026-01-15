package vn.web.fashionshop.controller;

import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import vn.web.fashionshop.dto.cart.CartDto;
import vn.web.fashionshop.dto.checkout.CheckoutForm;
import vn.web.fashionshop.entity.Order;
import vn.web.fashionshop.enums.EPaymentMethod;
import vn.web.fashionshop.service.CheckoutService;

import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.GuestCartService;
import vn.web.fashionshop.util.GuestCartCookieUtil;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final GuestCartService guestCartService;
    private final CheckoutService checkoutService;

    public CheckoutController(CartService cartService, GuestCartService guestCartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.guestCartService = guestCartService;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout")
    public String checkout(HttpServletRequest request, Model model) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        CartDto cart = shouldUseGuestCart(request)
                ? guestCartService.getGuestCart(request)
                : cartService.getMyCart();

        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            return "redirect:/cart";
        }

        CheckoutForm form = checkoutService.buildPrefilledForm(email);
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutForm", form);
        model.addAttribute("paymentMethods", EPaymentMethod.values());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        CartDto cart = shouldUseGuestCart(request)
                ? guestCartService.getGuestCart(request)
                : cartService.getMyCart();
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cart);
            model.addAttribute("paymentMethods", EPaymentMethod.values());
            return "checkout";
        }

        try {
            Long orderId = checkoutService.placeOrder(email, form, request, response);
            return "redirect:/checkout/success/" + orderId;
        } catch (IllegalArgumentException ex) {
            // For voucher errors and other user-facing validation issues.
            bindingResult.rejectValue("voucherCode", "invalid", ex.getMessage());
            model.addAttribute("cart", cart);
            model.addAttribute("paymentMethods", EPaymentMethod.values());
            return "checkout";
        }
    }

    @PostMapping("/checkout/preview")
    public String previewVoucher(
            @ModelAttribute("checkoutForm") CheckoutForm form,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            HttpServletRequest request,
            Model model) {

        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        CartDto cart = shouldUseGuestCart(request)
                ? guestCartService.getGuestCart(request)
                : cartService.getMyCart();
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            return "redirect:/cart";
        }

        // Keep voucherCode on form if posted as request param
        if (voucherCode != null) {
            form.setVoucherCode(voucherCode);
        }

        try {
            var preview = checkoutService.previewVoucher(form.getVoucherCode(), cart.subtotal());
            model.addAttribute("voucherPreview", preview);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("voucherError", ex.getMessage());
        }

        model.addAttribute("cart", cart);
        model.addAttribute("checkoutForm", form);
        model.addAttribute("paymentMethods", EPaymentMethod.values());
        return "checkout";
    }

    @GetMapping("/checkout/success/{id}")
    public String checkoutSuccess(@PathVariable("id") Long id, Model model) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        Order order = checkoutService.getMyOrderForSuccessPage(email, id);
        if (order == null) {
            return "redirect:/cart";
        }
        model.addAttribute("order", order);
        return "checkout-success";
    }

    private static boolean shouldUseGuestCart(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return !GuestCartCookieUtil.readGuestCart(request.getCookies()).isEmpty();
    }
}
