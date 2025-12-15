package vn.web.fashionshop.dto;

import lombok.Data;
import vn.web.fashionshop.enums.EGender;

@Data
public class RegisterDTO {
    private String fullName;
    private String email;
    private String phone;
    private EGender gender;
    private String address;
    private String password;
    private String confirmPassword;
}
