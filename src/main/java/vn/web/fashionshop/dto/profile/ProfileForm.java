package vn.web.fashionshop.dto.profile;

import jakarta.validation.constraints.NotBlank;
import vn.web.fashionshop.enums.EGender;

public class ProfileForm {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private EGender gender;

    private String address;

    public ProfileForm() {
    }

    public ProfileForm(String fullName, String phone, EGender gender, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public EGender getGender() {
        return gender;
    }

    public void setGender(EGender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
