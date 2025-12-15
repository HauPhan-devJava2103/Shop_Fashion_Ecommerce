package vn.web.fashionshop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ERoleName {

    CUSTOMER("Khách hàng"),
    ADMIN("Quản trị viên"),
    STAFF("Nhân viên");

    private final String displayName;
}
