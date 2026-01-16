# 🛍️ Fashion Shop E-Commerce

Hệ thống thương mại điện tử thời trang được xây dựng bằng Spring Boot 3.5.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📑 Mục Lục

- [Tính năng](#-tính-năng)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#️-cấu-hình)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Tác giả](#-tác-giả)

## ✨ Tính Năng

### 👤 Khách hàng

- 🔐 Đăng ký/Đăng nhập với xác thực OTP qua email
- 🔑 Quên mật khẩu và đặt lại qua email
- 🛒 Giỏ hàng (Shopping Cart)
- ❤️ Danh sách yêu thích (Wishlist)
- 📦 Quản lý đơn hàng và theo dõi trạng thái
- 💳 Thanh toán COD và VNPay
- ⭐ Đánh giá sản phẩm
- 💬 Chat hỗ trợ trực tuyến (WebSocket)
- 🎫 Sử dụng mã giảm giá (Voucher)
- 👤 Quản lý hồ sơ cá nhân

### 🔧 Quản trị viên (Admin)

- 📊 Dashboard thống kê doanh thu
- 📦 Quản lý sản phẩm (CRUD, hình ảnh, biến thể)
- 📂 Quản lý danh mục sản phẩm
- 👥 Quản lý người dùng
- 📋 Quản lý đơn hàng (xác nhận, cập nhật trạng thái)
- 💬 Hỗ trợ khách hàng qua chat realtime
- ⭐ Quản lý đánh giá sản phẩm
- 🎫 Quản lý voucher/mã giảm giá

### 🔧 Nhân viên (Staff)

- 📊 Dashboard thống kê doanh thu
- 📦 Quản lý sản phẩm (CRUD, hình ảnh, biến thể)
- 📂 Quản lý danh mục sản phẩm
- 📋 Quản lý đơn hàng (xác nhận, cập nhật trạng thái)
- 💬 Hỗ trợ khách hàng qua chat realtime
- ⭐ Quản lý đánh giá sản phẩm
- 🎫 Quản lý voucher/mã giảm giá

## 🛠 Công Nghệ Sử Dụng

| Layer              | Công nghệ                                           |
| ------------------ | --------------------------------------------------- |
| **Backend**        | Spring Boot 3.5.9, Spring Security, Spring Data JPA |
| **Frontend**       | Thymeleaf, Bootstrap, HTML/CSS, JavaScript          |
| **Database**       | MySQL 8.0                                           |
| **Authentication** | JWT (JSON Web Token)                                |
| **Email**          | Spring Mail (Gmail SMTP)                            |
| **Payment**        | VNPay Sandbox                                       |
| **Real-time Chat** | WebSocket (STOMP)                                   |
| **Build Tool**     | Maven                                               |
| **Other**          | Lombok, Thymeleaf Layout Dialect                    |

## 📋 Yêu Cầu Hệ Thống

- **Java**: JDK 17 trở lên
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **IDE**: IntelliJ IDEA / VS Code / Eclipse (khuyến nghị IntelliJ)

## 🚀 Cài Đặt

### 1. Clone repository

```bash
git clone https://github.com/HauPhan-devJava2103/Shop_Fashion_Ecommerce.git
```

### 2. Tạo database MySQL

```sql
CREATE DATABASE DoAnCntt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Import dữ liệu mẫu (tùy chọn)

```bash
mysql -u root -p DoAnCntt < data.sql
```

### 4. Cấu hình application.properties

Mở file `src/main/resources/application.properties` và cập nhật thông tin:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/DoAnCntt
spring.datasource.username=root
spring.datasource.password=your_password

# Email (Gmail) - Xem hướng dẫn bên dưới
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 5. Build dự án

```bash
# Windows
mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

## ⚙️ Cấu Hình

### Biến môi trường (Environment Variables)

| Biến                | Mô tả              | Bắt buộc             |
| ------------------- | ------------------ | -------------------- |
| `DB_URL`            | URL kết nối MySQL  | Không (có mặc định)  |
| `MAIL_USERNAME`     | Email gửi OTP      | ✅ Có                |
| `MAIL_PASSWORD`     | App Password Gmail | ✅ Có                |
| `VNPAY_TMN_CODE`    | Mã Terminal VNPay  | Không (dùng Sandbox) |
| `VNPAY_HASH_SECRET` | Secret key VNPay   | Không (dùng Sandbox) |
| `VNPAY_RETURN_URL`  | URL callback VNPay | Không (có mặc định)  |

### 📧 Cấu hình Gmail App Password

> ⚠️ **Quan trọng**: Gmail yêu cầu App Password thay vì mật khẩu thông thường.

1. Đăng nhập tài khoản Google
2. Bật **Xác thực 2 bước** tại: [Bảo mật Google](https://myaccount.google.com/security)
3. Truy cập: [Google App Passwords](https://myaccount.google.com/apppasswords)
4. Chọn **Mail** và **Windows Computer**
5. Nhấn **Generate** và sao chép mật khẩu 16 ký tự
6. Sử dụng mật khẩu này trong `MAIL_PASSWORD`

### 💳 Cấu hình VNPay (Môi trường Sandbox)

Dự án đã được cấu hình sẵn với thông tin Sandbox của VNPay. Để test thanh toán:

- Sử dụng thẻ test: `9704198526191432198`
- Tên chủ thẻ: `NGUYEN VAN A`
- Ngày phát hành: `07/15`
- Mật khẩu OTP: `123456`

## ▶️ Chạy Ứng Dụng

```bash
# Chạy với Maven
./mvnw spring-boot:run

# Hoặc chạy file JAR
java -jar target/fashionshop-0.0.1-SNAPSHOT.jar
```

Ứng dụng sẽ chạy tại: **http://localhost:8888**

### Tài khoản mặc định

| Role     | Email                 | Password |
| -------- | --------------------- | -------- |
| Admin    | admin@fashionshop.com | admin123 |
| Staff    | staff@fashionshop.com | staff123 |
| Customer | Tự đăng ký            | -        |

## 📁 Cấu Trúc Dự Án

```
src/main/java/vn/web/fashionshop/
├── 📂 config/           # Cấu hình (Security, WebSocket, WebMvc)
├── 📂 controller/       # Controllers
│   ├── admin/          # Admin portal controllers
│   └── api/            # REST API controllers
├── 📂 dto/              # Data Transfer Objects
├── 📂 entity/           # JPA Entities (User, Product, Order, ...)
├── 📂 enums/            # Enums (OrderStatus, Role, Gender, ...)
├── 📂 exception/        # Custom Exceptions & Handler
├── 📂 repository/       # JPA Repositories
├── 📂 security/         # JWT Filter, UserPrincipal
├── 📂 service/          # Business Logic Services
└── 📂 util/             # Utility classes

src/main/resources/
├── 📂 templates/        # Thymeleaf templates
│   ├── admin/          # Admin pages
│   ├── customer/       # Customer pages
│   ├── fragments/      # Reusable components
│   └── layouts/        # Layout templates
├── 📂 static/           # CSS, JS, Images
└── 📄 application.properties
```

## 🤝 Đóng Góp

1. Fork dự án
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit thay đổi (`git commit -m 'Add some AmazingFeature'`)
4. Push lên branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

## 👨‍💻 Tác Giả

- **Tên**: [Phan Phúc Hậu]
- **MSSV**: [23110097]
- **Email**: [23110097@student.hcmute.edu.vn]
- **Trường**: [Trường Đại học Công Nghệ Kỹ thuật TP.HCM]

- **Tên**: [Hà Trường Giang]
- **MSSV**: [23110095]
- **Email**: [23110095@student.hcmute.edu.vn]
- **Trường**: [Trường Đại học Công Nghệ Kỹ thuật TP.HCM]

## 📄 License

Dự án này được phát triển cho mục đích học tập - Đồ án Công nghệ Thông tin.

---

<p align="center">
  Made with ❤️ using Spring Boot
</p>

<p align="center">
  ⭐ Nếu dự án hữu ích, hãy cho một star nhé!
</p>
