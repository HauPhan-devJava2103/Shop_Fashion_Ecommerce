package vn.web.fashionshop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EChatMessageType {
    TEXT("Văn bản"),
    IMAGE("Hình ảnh"),
    SYSTEM("Hệ thống");

    private final String displayName;
}
