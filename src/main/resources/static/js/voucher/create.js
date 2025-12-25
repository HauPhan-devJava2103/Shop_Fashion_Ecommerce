// Live preview for voucher form
document.addEventListener("DOMContentLoaded", function () {
  const codeInput = document.getElementById("code");
  const descInput = document.getElementById("description");
  const percentInput = document.getElementById("discountPercent");
  const maxInput = document.getElementById("maxDiscountAmount");
  const minInput = document.getElementById("minOrderValue");

  if (codeInput) {
    codeInput.addEventListener("input", function () {
      document.getElementById("previewCode").textContent =
        this.value.toUpperCase() || "VOUCHER";
    });
  }

  if (descInput) {
    descInput.addEventListener("input", function () {
      document.getElementById("previewDesc").textContent =
        this.value || "Mô tả voucher";
    });
  }

  if (percentInput) {
    percentInput.addEventListener("input", function () {
      document.getElementById("previewPercent").textContent = this.value || "0";
    });
  }

  if (maxInput) {
    maxInput.addEventListener("input", function () {
      const val = this.value
        ? Number(this.value).toLocaleString("vi-VN") + "đ"
        : "---";
      document.getElementById("previewMax").textContent = val;
    });
  }

  if (minInput) {
    minInput.addEventListener("input", function () {
      const val = this.value
        ? Number(this.value).toLocaleString("vi-VN") + "đ"
        : "---";
      document.getElementById("previewMin").textContent = val;
    });
  }

  // Bootstrap validation
  var forms = document.querySelectorAll(".needs-validation");
  Array.prototype.slice.call(forms).forEach(function (form) {
    form.addEventListener(
      "submit",
      function (event) {
        if (!form.checkValidity()) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add("was-validated");
      },
      false
    );
  });
});
