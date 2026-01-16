package vn.web.fashionshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * VNPay Payment Gateway Configuration
 * Đọc cấu hình từ application.properties với prefix "vnpay"
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {

    /**
     * Terminal ID được VNPay cấp (vnp_TmnCode)
     */
    private String tmnCode;

    /**
     * Secret key để tạo checksum
     */
    private String hashSecret;

    /**
     * VNPay Payment URL
     * Sandbox: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
     * Production: https://pay.vnpay.vn/vpcpay.html
     */
    private String payUrl;

    /**
     * URL callback sau khi thanh toán
     */
    private String returnUrl;

    /**
     * VNPay API URL (cho query transaction)
     */
    private String apiUrl;
}
