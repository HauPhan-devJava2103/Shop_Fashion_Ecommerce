package vn.web.fashionshop.dto;

import java.time.LocalDateTime;

import lombok.Data;
import vn.web.fashionshop.enums.EGender;

@Data
public class PendingRegistration {
    private String email;
    private String fullName;
    private String phone;
    private EGender gender;
    private String address;
    private String encodedPassword;
    private String otpCode;
    private LocalDateTime expiryTime;

    public PendingRegistration() {
    }

    public PendingRegistration(String email, String fullName, String phone,
            EGender gender, String address,
            String encodedPassword, String otpCode, int expiryMinutes) {
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
        this.encodedPassword = encodedPassword;
        this.otpCode = otpCode;
        this.expiryTime = LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

}