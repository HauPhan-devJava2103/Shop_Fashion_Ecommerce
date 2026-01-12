package vn.web.fashionshop.service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.web.fashionshop.dto.PendingPasswordReset;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.repository.UserRepository;

@Service
public class PasswordResetService {

    // In-Memory storage: Key = email, Value = PendingPasswordReset
    private final Map<String, PendingPasswordReset> pendingResets = new ConcurrentHashMap<>();

    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public PasswordResetService(JavaMailSender mailSender, PasswordEncoder passwordEncoder,
            UserRepository userRepository) {
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // Generate 6-digit OTP
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Create OTP and send email for password reset
    public boolean createAndSendResetOtp(String email) {
        // Check if email exists
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return false;
        }

        String otpCode = generateOtp();

        PendingPasswordReset pending = new PendingPasswordReset(email, otpCode, otpExpirationMinutes);
        pendingResets.put(email, pending);

        sendResetOtpEmail(email, otpCode, user.get().getFullName());
        return true;
    }

    // Verify OTP for password reset
    public boolean verifyResetOtp(String email, String otpCode) {
        PendingPasswordReset pending = pendingResets.get(email);

        if (pending == null) {
            return false;
        }

        if (pending.isExpired()) {
            pendingResets.remove(email);
            return false;
        }

        return pending.getOtpCode().equals(otpCode);
    }

    // Reset password after OTP verification
    public boolean resetPassword(String email, String otpCode, String newPassword) {
        if (!verifyResetOtp(email, otpCode)) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove pending reset
        pendingResets.remove(email);

        return true;
    }

    // Resend OTP for password reset
    public boolean resendResetOtp(String email) {
        PendingPasswordReset existing = pendingResets.get(email);

        if (existing == null) {
            // Start fresh
            return createAndSendResetOtp(email);
        }

        // Generate new OTP
        String newOtpCode = generateOtp();
        PendingPasswordReset updated = new PendingPasswordReset(email, newOtpCode, otpExpirationMinutes);
        pendingResets.put(email, updated);

        Optional<User> user = userRepository.findByEmail(email);
        String fullName = user.map(User::getFullName).orElse("Khách hàng");
        sendResetOtpEmail(email, newOtpCode, fullName);

        return true;
    }

    // Check if email has pending reset
    public boolean hasPendingReset(String email) {
        PendingPasswordReset pending = pendingResets.get(email);
        if (pending == null) {
            return false;
        }
        if (pending.isExpired()) {
            pendingResets.remove(email);
            return false;
        }
        return true;
    }

    // Send password reset OTP email
    private void sendResetOtpEmail(String toEmail, String otpCode, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Fashion Shop - Đặt lại mật khẩu");
        message.setText(
                "Xin chào " + fullName + ",\n\n" +
                        "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản Fashion Shop.\n\n" +
                        "Mã OTP của bạn là: " + otpCode + "\n\n" +
                        "Mã này có hiệu lực trong " + otpExpirationMinutes + " phút.\n\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\nFashion Shop");
        mailSender.send(message);
    }
}
