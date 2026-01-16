package vn.web.fashionshop.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.web.fashionshop.config.VNPayConfig;
import vn.web.fashionshop.entity.Order;
import vn.web.fashionshop.entity.Payment;
import vn.web.fashionshop.entity.PaymentTransaction;
import vn.web.fashionshop.enums.EPaymentGateway;
import vn.web.fashionshop.enums.EPaymentStatus;
import vn.web.fashionshop.repository.PaymentRepository;
import vn.web.fashionshop.repository.PaymentTransactionRepository;

/**
 * VNPay Payment Service
 * Xử lý tạo URL thanh toán và xác thực callback
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    /**
     * DTO chứa kết quả xử lý callback
     */
    public record PaymentCallbackResult(
            boolean success,
            Long orderId,
            String errorCode,
            String errorMessage) {
    }

    /**
     * Tạo URL thanh toán VNPay
     * 
     * @param order   Đơn hàng cần thanh toán
     * @param request HttpServletRequest để lấy IP
     * @return URL redirect đến VNPay
     */
    public String createPaymentUrl(Order order, HttpServletRequest request) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(order.getId());
        String vnp_IpAddr = getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String orderType = "other";

        // Số tiền VNPay yêu cầu nhân 100 (không có phần thập phân)
        long amount = order.getTotalAmount().longValue() * 100;
        String vnp_Amount = String.valueOf(amount);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getId());
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Thời gian tạo giao dịch
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Thời gian hết hạn (15 phút)
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    log.error("Error encoding URL params", e);
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryUrl;
        log.info("VNPay Payment URL created for order #{}: {}", order.getId(), paymentUrl);

        return paymentUrl;
    }

    /**
     * Xác thực chữ ký từ VNPay callback
     * 
     * @param params Tất cả request params từ VNPay
     * @return true nếu chữ ký hợp lệ
     */
    public boolean verifyCallback(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }

        // Remove hash params
        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        // Sort & build hash data
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    log.error("Error encoding URL params", e);
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        return calculatedHash.equalsIgnoreCase(vnp_SecureHash);
    }

    /**
     * Kiểm tra response code từ VNPay
     * 
     * @param responseCode Mã phản hồi từ VNPay
     * @return true nếu thanh toán thành công
     */
    public boolean isPaymentSuccess(String responseCode) {
        return "00".equals(responseCode);
    }

    /**
     * Lấy thông báo lỗi từ response code
     */
    public String getErrorMessage(String responseCode) {
        return switch (responseCode) {
            case "00" -> "Giao dịch thành công";
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
            case "10" -> "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch";
            case "12" -> "Thẻ/Tài khoản bị khóa";
            case "13" -> "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư để thực hiện giao dịch";
            case "65" -> "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99" -> "Lỗi không xác định";
            default -> "Lỗi không xác định (Mã: " + responseCode + ")";
        };
    }

    /**
     * Xử lý callback từ VNPay và cập nhật Payment status
     * 
     * @param params Tất cả request params từ VNPay
     * @return PaymentCallbackResult chứa kết quả xử lý
     */
    @Transactional
    public PaymentCallbackResult processPaymentCallback(Map<String, String> params) {
        // Verify chữ ký
        if (!verifyCallback(params)) {
            log.error("VNPay callback signature verification failed");
            return new PaymentCallbackResult(false, null, "invalid_signature", "Chữ ký không hợp lệ");
        }

        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef"); // Đây là orderId
        String vnp_TransactionNo = params.get("vnp_TransactionNo");

        Long orderId;
        try {
            orderId = Long.parseLong(vnp_TxnRef);
        } catch (NumberFormatException e) {
            log.error("Invalid order ID from VNPay: {}", vnp_TxnRef);
            return new PaymentCallbackResult(false, null, "invalid_order", "Mã đơn hàng không hợp lệ");
        }

        // Tìm Payment theo orderId
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            log.error("Payment not found for order ID: {}", orderId);
            return new PaymentCallbackResult(false, orderId, "order_not_found", "Không tìm thấy đơn hàng");
        }

        LocalDateTime now = LocalDateTime.now();

        // Tạo PaymentTransaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPayment(payment);
        transaction.setGateway(EPaymentGateway.VNPAY);
        transaction.setTxnRef(vnp_TxnRef);
        transaction.setGatewayTxnId(vnp_TransactionNo);
        transaction.setResponseMessage(getErrorMessage(vnp_ResponseCode));
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        paymentTransactionRepository.save(transaction);

        // Cập nhật Payment status
        if (isPaymentSuccess(vnp_ResponseCode)) {
            payment.setStatus(EPaymentStatus.SUCCESS);
            payment.setPaidAt(now);
            payment.setUpdatedAt(now);
            paymentRepository.save(payment);

            log.info("Payment SUCCESS for order #{}", orderId);
            return new PaymentCallbackResult(true, orderId, vnp_ResponseCode, "Thanh toán thành công");
        } else {
            payment.setStatus(EPaymentStatus.FAILED);
            payment.setUpdatedAt(now);
            paymentRepository.save(payment);

            String errorMessage = getErrorMessage(vnp_ResponseCode);
            log.warn("Payment FAILED for order #{}: {}", orderId, errorMessage);
            return new PaymentCallbackResult(false, orderId, vnp_ResponseCode, errorMessage);
        }
    }

    /**
     * HMAC SHA512 hashing
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmacSha512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmacSha512.init(secretKeySpec);
            byte[] bytes = hmacSha512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            log.error("Error creating HMAC SHA512", e);
            throw new RuntimeException("Error creating HMAC SHA512", e);
        }
    }

    /**
     * Lấy IP address từ request
     */
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        // Xử lý trường hợp có nhiều IP (proxy chain)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        // Xử lý localhost IPv6
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }
}
