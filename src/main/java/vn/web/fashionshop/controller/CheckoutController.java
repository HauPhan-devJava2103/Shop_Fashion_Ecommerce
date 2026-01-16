package vn.web.fashionshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.CheckoutService;
import vn.web.fashionshop.service.GuestCartService;
import vn.web.fashionshop.service.OrderService;
import vn.web.fashionshop.service.VNPayService;
import vn.web.fashionshop.util.GuestCartCookieUtil;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final GuestCartService guestCartService;
    private final CheckoutService checkoutService;
    private final VNPayService vnPayService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, GuestCartService guestCartService,
            CheckoutService checkoutService, VNPayService vnPayService,
            OrderService orderService) {
        this.cartService = cartService;
        this.guestCartService = guestCartService;
        this.checkoutService = checkoutService;
        this.vnPayService = vnPayService;
        this.orderService = orderService;
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

            // Nếu chọn BANK_TRANSFER -> Redirect sang VNPay
            if (form.getPaymentMethod() == EPaymentMethod.BANK_TRANSFER) {
                Order order = orderService.getOrderById(orderId);
                if (order != null) {
                    String vnpayUrl = vnPayService.createPaymentUrl(order, request);
                    return "redirect:" + vnpayUrl;
                }
            }

            // COD -> Redirect thẳng đến success page
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

    /**
     * Trang hiển thị khi thanh toán VNPay thất bại
     */
    @GetMapping("/checkout/payment-failed/{orderId}")
    public String paymentFailed(
            @PathVariable("orderId") Long orderId,
            @RequestParam(value = "error", required = false) String errorCode,
            Model model) {

        model.addAttribute("orderId", orderId);
        model.addAttribute("errorCode", errorCode);

        // Map error code to message
        String errorMessage = switch (errorCode != null ? errorCode : "") {
            case "00" -> "Giao dịch thành công";
            case "07" -> "Giao dịch bị nghi ngờ (liên quan tới lừa đảo)";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký InternetBanking";
            case "10" -> "Xác thực thông tin thẻ không đúng quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán";
            case "12" -> "Thẻ/Tài khoản bị khóa";
            case "13" -> "Nhập sai mật khẩu xác thực (OTP)";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Tài khoản vượt quá hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Nhập sai mật khẩu thanh toán quá số lần quy định";
            default -> "Đã có lỗi xảy ra trong quá trình thanh toán";
        };
        model.addAttribute("errorMessage", errorMessage);

        return "payment-failed";
    }

    private static boolean shouldUseGuestCart(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return !GuestCartCookieUtil.readGuestCart(request.getCookies()).isEmpty();
    }
}
