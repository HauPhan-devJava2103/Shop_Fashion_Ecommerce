# Implementation Plan: Update Product Feature

## 📋 Overview

Chức năng update/edit product cho phép admin chỉnh sửa thông tin sản phẩm bao gồm: thông tin cơ bản, hình ảnh, variants, và category.

---

## 🏗️ Architecture & Components

### **1. Backend Components**

- **Controller**: `ProductController.java` - Handle GET/POST requests
- **Service**: `ProductService.java` - Business logic
- **Repository**: `ProductRepository.java` - Database operations
- **Entity**: `Product.java`, `ProductImage.java`, `ProductVariant.java`

### **2. Frontend Components**

- **Template**: `admin/product/edit.html` - Edit form UI
- **JavaScript**: Form validation, image preview, variant management
- **CSS**: Custom styling (if needed)

---

## 📝 Step-by-Step Implementation

### **STEP 1: Controller - GET Edit Page**

**File**: `ProductController.java`

```java
// GET /admin/products/edit/{id}
@GetMapping("/edit/{id}")
public String editProductForm(@PathVariable Long id, Model model) {
    // Get product with all relationships
    Product product = productService.getProductById(id);

    // Get all categories for dropdown
    List<Category> categories = categoryService.getAll();

    // Add to model
    model.addAttribute("product", product);
    model.addAttribute("categories", categories);

    return "admin/product/edit";
}
```

**✅ Action Items:**

1. Add `@GetMapping("/edit/{id}")` method to ProductController
2. Fetch product by ID from service
3. Fetch all categories for dropdown selection
4. Pass data to view via model
5. Return view name "admin/product/edit"

---

### **STEP 2: Controller - POST Update Product**

**File**: `ProductController.java`

```java
// POST /admin/products/edit/{id}
@PostMapping("/edit/{id}")
public String updateProduct(
        @PathVariable Long id,
        @Valid @ModelAttribute ProductUpdateDTO productDTO,
        BindingResult result,
        @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
        RedirectAttributes redirectAttributes) {

    // Validate form
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin!");
        return "redirect:/admin/products/edit/" + id;
    }

    try {
        // Update product
        productService.updateProduct(id, productDTO, imageFiles);

        redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        return "redirect:/admin/products/" + id;

    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        return "redirect:/admin/products/edit/" + id;
    }
}
```

**✅ Action Items:**

1. Add `@PostMapping("/edit/{id}")` method
2. Bind form data to ProductUpdateDTO
3. Validate with `@Valid` and check `BindingResult`
4. Handle MultipartFile[] for images
5. Call productService.updateProduct()
6. Add flash messages for success/error
7. Redirect to product view or back to edit

---

### **STEP 3: Create ProductUpdateDTO**

**File**: Create new `ProductUpdateDTO.java` in dto package

```java
package vn.web.fashionshop.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductUpdateDTO {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255)
    private String productName;

    @NotBlank(message = "SKU không được để trống")
    @Size(max = 50)
    private String sku;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0)
    private Double price;

    @Min(value = 0)
    @Max(value = 100)
    private Integer discount;

    @Min(value = 0)
    private Integer stock;

    @Size(max = 5000)
    private String description;

    private Boolean isActive;
}
```

**✅ Action Items:**

1. Create ProductUpdateDTO class
2. Add fields for all editable properties
3. Add validation annotations
4. Use Lombok @Data or generate getters/setters

---

### **STEP 4: Service - Update Product Logic**

**File**: `ProductService.java`

```java
@Transactional
public void updateProduct(Long id, ProductUpdateDTO dto, MultipartFile[] imageFiles) throws IOException {
    // 1. Find existing product
    Product product = getProductById(id);

    // 2. Update basic fields
    product.setProductName(dto.getProductName());
    product.setSku(dto.getSku());
    product.setPrice(dto.getPrice());
    product.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : 0);
    product.setStock(dto.getStock() != null ? dto.getStock() : 0);
    product.setDescription(dto.getDescription());
    product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : false);
    product.setUpdatedAt(LocalDateTime.now());

    // 3. Update category
    if (dto.getCategoryId() != null) {
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);
    }

    // 4. Handle image upload (if provided)
    if (imageFiles != null && imageFiles.length > 0) {
        handleImageUpload(product, imageFiles);
    }

    // 5. Save product
    productRepository.save(product);
}
```

**✅ Action Items:**

1. Add updateProduct() method with @Transactional
2. Fetch existing product by ID
3. Update all editable fields from DTO
4. Update category relationship
5. Handle image upload (optional, for later)
6. Call productRepository.save()

---

### **STEP 5: HTML Template - Basic Structure**

**File**: Create `admin/product/edit.html`

```html
<!DOCTYPE html>
<html
  xmlns:th="http://www.thymeleaf.org"
  xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
  layout:decorate="~{layout/admin}">
  <head>
    <title>Chỉnh sửa sản phẩm - [[${product.productName}]]</title>
  </head>
  <body>
    <div layout:fragment="content">
      <!-- Page Header -->
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 class="h3 fw-bold">
            <i class="bi bi-pencil-square text-primary me-2"></i>
            Chỉnh sửa sản phẩm
          </h1>
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-0">
              <li class="breadcrumb-item">
                <a th:href="@{/admin}">Dashboard</a>
              </li>
              <li class="breadcrumb-item">
                <a th:href="@{/admin/products}">Sản phẩm</a>
              </li>
              <li class="breadcrumb-item active">Chỉnh sửa</li>
            </ol>
          </nav>
        </div>
        <div>
          <a
            th:href="@{/admin/products/{id}(id=${product.id})}"
            class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-2"></i>Quay lại
          </a>
        </div>
      </div>

      <!-- Flash Messages -->
      <div th:if="${success}" class="alert alert-success alert-dismissible">
        <i class="bi bi-check-circle me-2"></i>
        <span th:text="${success}"></span>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="alert"></button>
      </div>

      <div th:if="${error}" class="alert alert-danger alert-dismissible">
        <i class="bi bi-exclamation-triangle me-2"></i>
        <span th:text="${error}"></span>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="alert"></button>
      </div>

      <!-- Edit Form -->
      <form
        th:action="@{/admin/products/edit/{id}(id=${product.id})}"
        method="post"
        class="needs-validation"
        novalidate>
        <div class="row g-4">
          <!-- LEFT COLUMN -->
          <div class="col-lg-8">
            <!-- Basic Info Card -->
            <div class="card border-0 shadow-sm mb-4">
              <div class="card-header bg-white py-3">
                <h5 class="mb-0 fw-semibold">
                  <i class="bi bi-info-circle text-primary me-2"></i>
                  Thông tin cơ bản
                </h5>
              </div>
              <div class="card-body">
                <div class="row g-3">
                  <!-- Product Name -->
                  <div class="col-12">
                    <label for="productName" class="form-label fw-semibold">
                      Tên sản phẩm <span class="text-danger">*</span>
                    </label>
                    <input
                      type="text"
                      class="form-control"
                      id="productName"
                      name="productName"
                      th:value="${product.productName}"
                      required />
                  </div>

                  <!-- SKU & Category -->
                  <div class="col-md-6">
                    <label for="sku" class="form-label fw-semibold">
                      SKU <span class="text-danger">*</span>
                    </label>
                    <input
                      type="text"
                      class="form-control"
                      id="sku"
                      name="sku"
                      th:value="${product.sku}"
                      required />
                  </div>

                  <div class="col-md-6">
                    <label for="categoryId" class="form-label fw-semibold">
                      Danh mục <span class="text-danger">*</span>
                    </label>
                    <select
                      class="form-select"
                      id="categoryId"
                      name="categoryId"
                      required>
                      <option
                        th:each="category : ${categories}"
                        th:value="${category.id}"
                        th:text="${category.categoryName}"
                        th:selected="${category.id == product.category.id}"></option>
                    </select>
                  </div>

                  <!-- Price, Discount, Stock -->
                  <div class="col-md-4">
                    <label for="price" class="form-label fw-semibold">
                      Giá <span class="text-danger">*</span>
                    </label>
                    <input
                      type="number"
                      class="form-control"
                      id="price"
                      name="price"
                      th:value="${product.price}"
                      min="0"
                      required />
                  </div>

                  <div class="col-md-4">
                    <label for="discount" class="form-label fw-semibold"
                      >Giảm giá (%)</label
                    >
                    <input
                      type="number"
                      class="form-control"
                      id="discount"
                      name="discount"
                      th:value="${product.discount}"
                      min="0"
                      max="100" />
                  </div>

                  <div class="col-md-4">
                    <label for="stock" class="form-label fw-semibold"
                      >Tồn kho</label
                    >
                    <input
                      type="number"
                      class="form-control"
                      id="stock"
                      name="stock"
                      th:value="${product.stock}"
                      min="0" />
                  </div>

                  <!-- Description -->
                  <div class="col-12">
                    <label for="description" class="form-label fw-semibold"
                      >Mô tả</label
                    >
                    <textarea
                      class="form-control"
                      id="description"
                      name="description"
                      rows="5"
                      th:text="${product.description}"></textarea>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- RIGHT COLUMN -->
          <div class="col-lg-4">
            <!-- Status Card -->
            <div class="card border-0 shadow-sm mb-4">
              <div class="card-header bg-white py-3">
                <h5 class="mb-0 fw-semibold">
                  <i class="bi bi-gear text-primary me-2"></i>Trạng thái
                </h5>
              </div>
              <div class="card-body">
                <div class="form-check form-switch">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    id="isActive"
                    name="isActive"
                    th:checked="${product.isActive}" />
                  <label class="form-check-label" for="isActive">
                    Đang hoạt động
                  </label>
                </div>

                <hr class="my-3" />

                <div class="mb-2">
                  <label class="text-muted small fw-semibold">Ngày tạo</label>
                  <p
                    class="mb-0"
                    th:text="${#temporals.format(product.createdAt, 'dd/MM/yyyy HH:mm')}"></p>
                </div>

                <div th:if="${product.updatedAt != null}">
                  <label class="text-muted small fw-semibold"
                    >Cập nhật lần cuối</label
                  >
                  <p
                    class="mb-0"
                    th:text="${#temporals.format(product.updatedAt, 'dd/MM/yyyy HH:mm')}"></p>
                </div>
              </div>
            </div>

            <!-- Action Buttons Card -->
            <div class="card border-0 shadow-sm">
              <div class="card-body d-grid gap-2">
                <button type="submit" class="btn btn-primary btn-lg">
                  <i class="bi bi-check-circle me-2"></i>Lưu thay đổi
                </button>
                <a
                  th:href="@{/admin/products/{id}(id=${product.id})}"
                  class="btn btn-outline-secondary">
                  <i class="bi bi-x-circle me-2"></i>Hủy
                </a>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>

    <!-- Validation Script -->
    <script layout:fragment="scripts">
      // Bootstrap form validation
      (function () {
        "use strict";
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
      })();
    </script>
  </body>
</html>
```

**✅ Action Items:**

1. Create edit.html template file
2. Add page header with breadcrumb
3. Add flash message alerts
4. Create 2-column form layout (8-4 grid)
5. Add all input fields with th:value for existing data
6. Add validation script
7. Add submit & cancel buttons

---

## 🎯 Testing Steps

1. **Navigate**: Go to `/admin/products/edit/1`
2. **Check Form**: All fields populated with current data
3. **Edit**: Change product name, price, etc.
4. **Submit**: Click "Lưu thay đổi"
5. **Verify**: Redirects to product detail with success message
6. **Check DB**: Data updated in database
7. **Test Validation**: Leave required fields empty, check errors

---

## 📚 Implementation Order

1. ✅ Backend GET endpoint (STEP 1)
2. ✅ Create ProductUpdateDTO (STEP 3)
3. ✅ Backend POST endpoint (STEP 2)
4. ✅ Service updateProduct method (STEP 4)
5. ✅ HTML template basic form (STEP 5)
6. ✅ Test basic functionality
7. ⏳ Add image upload (later)
8. ⏳ Add variant management (later)

---

## ⚠️ Important Notes

- Use `@Transactional` for update operations
- Add proper error handling with try-catch
- Use RedirectAttributes for flash messages
- Validate on both client and server side
- Test with different data scenarios
- Handle null values properly

---

## 🔄 Future Enhancements

After basic edit is working:

1. Add image upload & management
2. Add variant editing
3. Add bulk field update
4. Add product duplication feature
5. Add audit log
