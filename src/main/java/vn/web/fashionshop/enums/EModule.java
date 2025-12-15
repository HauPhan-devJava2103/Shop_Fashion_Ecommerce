package vn.web.fashionshop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EModule {
    USER("Quản lý người dùng"),
    PRODUCT("Quản lý sản phẩm"),
    ORDER("Quản lý đơn hàng"),
    CATEGORY("Quản lý danh mục"),
    REVIEW("Quản lý đánh giá"),
    REPORT("Báo cáo thống kê");

    private final String displayName;
}