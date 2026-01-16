package vn.web.fashionshop.dto.checkout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.web.fashionshop.enums.EPaymentMethod;

@Data
public class CheckoutForm {

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
    private String recipientName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @NotBlank(message = "Vui lòng nhập địa chỉ")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String addressLine;

    private String note;

    @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
    private String voucherCode;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private EPaymentMethod paymentMethod;
}
