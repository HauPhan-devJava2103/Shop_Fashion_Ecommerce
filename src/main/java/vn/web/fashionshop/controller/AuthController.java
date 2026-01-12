package vn.web.fashionshop.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import vn.web.fashionshop.dto.ApiResponse;
import vn.web.fashionshop.dto.ForgotPasswordDTO;
import vn.web.fashionshop.dto.PendingRegistration;
import vn.web.fashionshop.dto.RegisterDTO;
import vn.web.fashionshop.dto.ResetPasswordDTO;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.enums.ERoleName;
import vn.web.fashionshop.service.OtpService;
import vn.web.fashionshop.service.PasswordResetService;
import vn.web.fashionshop.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, OtpService otpService,
            PasswordResetService passwordResetService) {
        this.userService = userService;
        this.otpService = otpService;
        this.passwordResetService = passwordResetService;
    }

    // LOGIN

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    // REGISTER

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("registerUser", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerUser") RegisterDTO registerDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Check password confirmation
        if (registerDTO.getPassword() == null ||
                !registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "register";
        }

        // Check if email already exists
        if (userService.checkEmailExist(registerDTO.getEmail())) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return "register";
        }

        // Check if phone already exists
        if (userService.checkPhoneExist(registerDTO.getPhone())) {
            model.addAttribute("error", "Số điện thoại đã được sử dụng!");
            return "register";
        }

        // Create OTP and send email
        otpService.createAndSendOtp(registerDTO);

        // Redirect to verify-otp with email in URL (stateless)
        return "redirect:/verify-otp?email=" + registerDTO.getEmail();
    }

    // OTP VERIFICATION (Registration)

    @GetMapping("/verify-otp")
    public String showOtpPage(@RequestParam(required = false) String email, Model model) {
        if (email == null || email.isBlank() || !otpService.hasPendingRegistration(email)) {
            return "redirect:/register";
        }

        model.addAttribute("email", email);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
            @RequestParam String otpCode,
            Model model) {

        Optional<PendingRegistration> verified = otpService.verifyOtp(email, otpCode);

        if (verified.isEmpty()) {
            model.addAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn!");
            model.addAttribute("email", email);
            return "verify-otp";
        }

        // Create actual user from verified data
        PendingRegistration data = verified.get();

        User user = new User();
        user.setEmail(data.getEmail());
        user.setFullName(data.getFullName());
        user.setPhone(data.getPhone());
        user.setGender(data.getGender());
        user.setAddress(data.getAddress());
        user.setPassword(data.getEncodedPassword()); // Already BCrypt encoded
        user.setIsActive(true);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setRole(userService.getRoleByName(ERoleName.CUSTOMER));

        userService.saveDirectly(user);

        return "redirect:/login?registered=true";
    }

    // RESEND OTP (API - Registration)

    @PostMapping("/resend-otp")
    @ResponseBody
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        boolean success = otpService.resendOtp(email);

        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Đã gửi lại mã OTP!"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không tìm thấy yêu cầu đăng ký. Vui lòng đăng ký lại."));
        }
    }

    // FORGOT PASSWORD

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("forgotPassword", new ForgotPasswordDTO());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPassword") ForgotPasswordDTO dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }

        boolean sent = passwordResetService.createAndSendResetOtp(dto.getEmail());

        if (!sent) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
            return "forgot-password";
        }

        return "redirect:/reset-password?email=" + dto.getEmail();
    }

    // RESET PASSWORD

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam(required = false) String email, Model model) {
        if (email == null || email.isBlank()) {
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email);
        model.addAttribute("resetPassword", new ResetPasswordDTO());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetPassword") ResetPasswordDTO dto,
            BindingResult bindingResult,
            Model model) {

        model.addAttribute("email", dto.getEmail());

        if (bindingResult.hasErrors()) {
            return "reset-password";
        }

        // Check password confirmation
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "reset-password";
        }

        // Reset password
        boolean success = passwordResetService.resetPassword(dto.getEmail(), dto.getOtpCode(), dto.getNewPassword());

        if (!success) {
            model.addAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn!");
            return "reset-password";
        }

        return "redirect:/login?reset=true";
    }

    // RESEND OTP (API - Password Reset)

    @PostMapping("/resend-reset-otp")
    @ResponseBody
    public ResponseEntity<?> resendResetOtp(@RequestParam String email) {
        boolean success = passwordResetService.resendResetOtp(email);

        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Đã gửi lại mã OTP!"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể gửi lại mã OTP. Vui lòng thử lại."));
        }
    }
}
