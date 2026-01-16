package vn.web.fashionshop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.web.fashionshop.service.VNPayService;
import vn.web.fashionshop.service.VNPayService.PaymentCallbackResult;

/**
 * Controller xử lý callback từ VNPay
 */
@Controller
@RequestMapping("/api/payment/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayController {

    private final VNPayService vnPayService;

    /**
     * Callback URL - VNPay redirect về đây sau khi thanh toán
     */
    @GetMapping("/callback")
    public String vnpayCallback(HttpServletRequest request) {
        log.info("VNPay callback received");

        // Lấy tất cả params từ VNPay
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        log.info("VNPay callback params: {}", params);

        // Delegate xử lý cho service layer
        PaymentCallbackResult result = vnPayService.processPaymentCallback(params);

        if (result.success()) {
            return "redirect:/checkout/success/" + result.orderId();
        } else {
            if (result.orderId() != null) {
                return "redirect:/checkout/payment-failed/" + result.orderId() + "?error=" + result.errorCode();
            } else {
                return "redirect:/checkout/payment-failed?error=" + result.errorCode();
            }
        }
    }
}
