package vn.web.fashionshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.web.fashionshop.dto.PendingRegistration;
import vn.web.fashionshop.dto.RegisterDTO;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // In-Memory storage: Key = email, Value = PendingRegistration
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public OtpService(JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    // Generate 6-digit OTP
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 100000 - 999999
        return String.valueOf(otp);
    }

    // Create OTP, store pending registration, and send email
    public void createAndSendOtp(RegisterDTO dto) {
        String otpCode = generateOtp();

        PendingRegistration pending = new PendingRegistration(
                dto.getEmail(),
                dto.getFullName(),
                dto.getPhone(),
                dto.getGender(),
                dto.getAddress(),
                passwordEncoder.encode(dto.getPassword()), // Encode password now
                otpCode,
                otpExpirationMinutes);

        // Store in memory
        pendingRegistrations.put(dto.getEmail(), pending);

        // Send OTP email
        sendOtpEmail(dto.getEmail(), otpCode);
    }

    // Verify OTP
    public Optional<PendingRegistration> verifyOtp(String email, String otpCode) {
        PendingRegistration pending = pendingRegistrations.get(email);

        if (pending == null) {
            return Optional.empty();
        }

        if (pending.isExpired()) {
            pendingRegistrations.remove(email); // Cleanup expired
            return Optional.empty();
        }

        if (!pending.getOtpCode().equals(otpCode)) {
            return Optional.empty();
        }

        // Valid! Remove from pending and return data
        pendingRegistrations.remove(email);
        return Optional.of(pending);
    }

    // Resend OTP with new code
    public boolean resendOtp(String email) {
        PendingRegistration existing = pendingRegistrations.get(email);

        if (existing == null) {
            return false;
        }

        // Generate new OTP and update expiry
        String newOtpCode = generateOtp();
        PendingRegistration updated = new PendingRegistration(
                existing.getEmail(),
                existing.getFullName(),
                existing.getPhone(),
                existing.getGender(),
                existing.getAddress(),
                existing.getEncodedPassword(),
                newOtpCode,
                otpExpirationMinutes);

        pendingRegistrations.put(email, updated);
        sendOtpEmail(email, newOtpCode);
        return true;
    }

    // Check if email has pending registration
    public boolean hasPendingRegistration(String email) {
        return pendingRegistrations.containsKey(email);
    }

    // Send OTP email
    private void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Fashion Shop - Mã xác thực OTP");
        message.setText(
                "Xin chào,\n\n" +
                        "Mã OTP của bạn là: " + otpCode + "\n\n" +
                        "Mã này có hiệu lực trong " + otpExpirationMinutes + " phút.\n\n" +
                        "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.\n\n" +
                        "Trân trọng,\nFashion Shop");
        mailSender.send(message);
    }
}