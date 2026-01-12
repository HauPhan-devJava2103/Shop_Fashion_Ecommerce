package vn.web.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String role;
    private String fullName;

    public LoginResponse(String token, String email, String role, String fullName) {
        this.token = token;
        this.type = "Bearer";
        this.email = email;
        this.role = role;
        this.fullName = fullName;
    }
}
