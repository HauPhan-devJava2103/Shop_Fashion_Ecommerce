package vn.web.fashionshop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EGender {
    MALE("Nam"),
    FEMALE("Nữ");

    private final String displayName;

}
