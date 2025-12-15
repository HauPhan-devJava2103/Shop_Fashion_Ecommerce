package vn.web.fashionshop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EPaymentStatus {
    PENDING("Chờ thanh toán"),
    SUCCESS("Thành công"),
    FAILED("Thất bại"),
    REFUNDED("Đã hoàn tiền");

    private final String displayName;
}