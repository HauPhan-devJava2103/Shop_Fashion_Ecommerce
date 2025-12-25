package vn.web.fashionshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.web.fashionshop.enums.EGender;

@Data
public class RegisterDTO {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên quá dài")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 8, max = 20, message = "Số điện thoại không hợp lệ")
    private String phone;

    private EGender gender;

    @Size(max = 500, message = "Địa chỉ quá dài")
    private String address;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String confirmPassword;
}
