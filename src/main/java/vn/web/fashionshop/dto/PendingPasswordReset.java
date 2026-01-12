package vn.web.fashionshop.dto;

import java.time.LocalDateTime;

/**
 * Holds pending password reset data in memory
 */
public class PendingPasswordReset {

    private String email;
    private String otpCode;
    private LocalDateTime expiryTime;

    public PendingPasswordReset(String email, String otpCode, int expirationMinutes) {
        this.email = email;
        this.otpCode = otpCode;
        this.expiryTime = LocalDateTime.now().plusMinutes(expirationMinutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public String getEmail() {
        return email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
}
