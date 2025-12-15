package vn.web.fashionshop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EPaymentGateway {
    MOMO("Ví MoMo"),
    VNPAY("VNPay"),
    ZALOPAY("ZaloPay");

    private final String displayName;
}