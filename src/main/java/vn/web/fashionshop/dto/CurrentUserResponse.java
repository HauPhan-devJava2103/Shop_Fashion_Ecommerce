package vn.web.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.web.fashionshop.enums.EGender;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private EGender gender;
    private String address;
    private String avatarUrl;
    private String role;
    private Boolean isActive;
}
