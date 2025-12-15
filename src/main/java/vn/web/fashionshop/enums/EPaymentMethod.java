package vn.web.fashionshop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EPaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    BANK_TRANSFER("Chuyển khoản ngân hàng");

    private final String displayName;
}