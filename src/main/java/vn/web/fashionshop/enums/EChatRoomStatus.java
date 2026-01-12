package vn.web.fashionshop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EChatRoomStatus {
    WAITING("Chờ nhân viên"),
    ACTIVE("Đang hoạt động"),
    CLOSED("Đã đóng");

    private final String displayName;
}
