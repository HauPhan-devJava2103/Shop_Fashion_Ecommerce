package vn.web.fashionshop.enums;

public enum EOrderCancelReason {
    CHANGE_MIND("Đổi ý / không muốn mua nữa"),
    ORDERED_WRONG("Đặt nhầm sản phẩm / size / màu"),
    FOUND_CHEAPER("Tìm được giá rẻ hơn"),
    DELIVERY_TOO_SLOW("Giao hàng quá lâu"),
    PAYMENT_ISSUE("Vấn đề thanh toán"),
    OTHER("Lý do khác");

    private final String displayName;

    EOrderCancelReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
