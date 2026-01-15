package vn.web.fashionshop.controller.api;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import vn.web.fashionshop.dto.ApiResponse;
import vn.web.fashionshop.dto.CurrentUserResponse;
import vn.web.fashionshop.dto.LoginRequest;
import vn.web.fashionshop.dto.LoginResponse;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.repository.UserRepository;
import vn.web.fashionshop.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthApiController(AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            boolean rememberMe = request.getRememberMe() != null && request.getRememberMe();
            String token = jwtUtil.generateToken(userDetails, rememberMe);

                // Save token to cookie for web browser
                // NOTE: SameSite=Lax helps mitigate CSRF when using cookie-based auth.
                ResponseCookie jwtCookie = ResponseCookie.from("jwt_token", token)
                    .httpOnly(true)
                    .secure(httpRequest.isSecure())
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(rememberMe ? Duration.ofDays(30) : Duration.ofDays(1))
                    .build();
                response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            // Get user info
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            String fullName = user != null ? user.getFullName() : "";
            String role = jwtUtil.extractRole(token);

            LoginResponse loginResponse = new LoginResponse(token, request.getEmail(), role, fullName);
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", loginResponse));

        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Tài khoản đã bị vô hiệu hóa"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Email hoặc mật khẩu không đúng"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest, HttpServletResponse response) {
        // Clear cookie
        ResponseCookie jwtCookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(httpRequest.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Chưa xác thực"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy user"));
        }

        String roleName = (user.getRole() != null && user.getRole().getRoleName() != null)
            ? user.getRole().getRoleName().name()
            : null;
        CurrentUserResponse dto = new CurrentUserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            user.getGender(),
            user.getAddress(),
            user.getAvatarUrl(),
            roleName,
            user.getIsActive());

        return ResponseEntity.ok(ApiResponse.success("Thành công", dto));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.success("Token hợp lệ"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token không hợp lệ"));
    }
}
