-- CREATE ROLE
INSERT INTO roles (role_name, description, is_active, created_at, updated_at) VALUES
('ADMIN', 'Quản trị viên hệ thống', true, NOW(), NOW()),
('CUSTOMER', 'Khách hàng', true, NOW(), NOW()),
('STAFF', 'Nhân viên', true, NOW(), NOW());

-- CREATE Permission
INSERT INTO permissions (permission_name, description, is_active, api_path, method, module, created_at, updated_at) VALUES
('VIEW_PRODUCTS', 'Xem danh sách sản phẩm', true, '/api/products', 'GET', 'PRODUCT', NOW(), NOW()),
('MANAGE_PRODUCTS', 'Quản lý sản phẩm', true, '/api/products/**', 'POST,PUT,DELETE', 'PRODUCT', NOW(), NOW()),
('VIEW_ORDERS', 'Xem đơn hàng', true, '/api/orders', 'GET', 'ORDER', NOW(), NOW()),
('MANAGE_ORDERS', 'Quản lý đơn hàng', true, '/api/orders/**', 'POST,PUT', 'ORDER', NOW(), NOW()),
('VIEW_USERS', 'Xem người dùng', true, '/api/users', 'GET', 'USER', NOW(), NOW()),
('MANAGE_USERS', 'Quản lý người dùng', true, '/api/users/**', 'POST,PUT,DELETE', 'USER', NOW(), NOW());

-- CREATE Role_Permission
-- 1. Xóa dữ liệu cũ trong role_permissions để tránh lỗi duplicate (nếu có)
DELETE FROM role_permissions;

-- 2. Gán tất cả quyền cho ADMIN (role_id = 1)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;
-- → ADMIN sẽ có quyền id 7,8,9,10,11,12

-- 3. Gán quyền tối thiểu cho CUSTOMER (role_id = 2)
-- Chỉ được xem sản phẩm và xem đơn hàng của mình
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 7),  -- VIEW_PRODUCTS
(2, 9);  -- VIEW_ORDERS

-- 4. (Tùy chọn) Gán quyền cho STAFF nếu cần, ví dụ: xem và quản lý đơn hàng, xem sản phẩm
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 7),   -- VIEW_PRODUCTS
(3, 9),   -- VIEW_ORDERS
(3, 10);  -- MANAGE_ORDERS

-- CREATE USER
INSERT INTO users (full_name, email, phone, gender, address, password, is_active, created_at, updated_at, role_id) VALUES
('Admin', 'admin@fashionshop.com', '0901234567', 'MALE', 'Hà Nội', '$2a$10$W7z9Y9fZfZfZfZfZfZfZfOu.examplehashedpassword', true, NOW(), NOW(), 1),
('Nguyễn Văn A', 'nguyenvana@gmail.com', '0912345678', 'MALE', 'TP.HCM', '$2a$10$examplehashed12345678901234567890', true, NOW(), NOW(), 2),
('Trần Thị B', 'tranthib@gmail.com', '0923456789', 'FEMALE', 'Đà Nẵng', '$2a$10$examplehashed12345678901234567890', true, NOW(), NOW(), 2),
('Lê Văn C', 'levanc@yahoo.com', '0934567890', 'MALE', 'Hà Nội', '$2a$10$examplehashed12345678901234567890', true, NOW(), NOW(), 2);

-- CREATE CATEGORY
INSERT INTO categories (category_name, slug, image_url, is_active, created_at, updated_at, parent_id) VALUES
('Áo', 'ao', 'https://example.com/cat-ao.jpg', true, NOW(), NOW(), NULL),
('Quần', 'quan', 'https://example.com/cat-quan.jpg', true, NOW(), NOW(), NULL),
('Váy', 'vay', 'https://example.com/cat-vay.jpg', true, NOW(), NOW(), NULL),
('Áo thun', 'ao-thun', 'https://example.com/cat-aothun.jpg', true, NOW(), NOW(), 1),
('Áo sơ mi', 'ao-so-mi', 'https://example.com/cat-aosomi.jpg', true, NOW(), NOW(), 1),
('Quần jeans', 'quan-jeans', 'https://example.com/cat-jeans.jpg', true, NOW(), NOW(), 2);

-- CREATE PRODUCT
INSERT INTO products (sku, product_name, description, stock, price, discount, is_active, created_at, updated_at, category_id) VALUES
('SP001', 'Áo thun basic trắng', 'Áo thun cotton thoải mái', 100, 199000.00, 10.00, true, NOW(), NOW(), 4),
('SP002', 'Áo sơ mi kẻ caro', 'Áo sơ mi công sở', 50, 450000.00, 0.00, true, NOW(), NOW(), 5),
('SP003', 'Quần jeans slimfit', 'Quần jeans co giãn', 80, 599000.00, 15.00, true, NOW(), NOW(), 6),
('SP004', 'Váy maxi hoa', 'Váy maxi nhẹ nhàng mùa hè', 40, 699000.00, 20.00, true, NOW(), NOW(), 3);

-- CREATE PRODUCT_VARIANT
INSERT INTO product_variants (product_id, size, color, stock, sku_variant, created_at, updated_at) VALUES
(1, 'M', 'Trắng', 30, 'SP001-M-TRANG', NOW(), NOW()),
(1, 'L', 'Trắng', 40, 'SP001-L-TRANG', NOW(), NOW()),
(1, 'XL', 'Trắng', 30, 'SP001-XL-TRANG', NOW(), NOW()),
(2, 'M', 'Xanh kẻ', 20, 'SP002-M-XANH', NOW(), NOW()),
(2, 'L', 'Xanh kẻ', 30, 'SP002-L-XANH', NOW(), NOW());


-- IMAGES
INSERT INTO images (product_id, url_image, alt_text, is_main, created_at, updated_at) VALUES
(1, 'https://example.com/product1-1.jpg', 'Áo thun trắng mặt trước', true, NOW(), NOW()),
(1, 'https://example.com/product1-2.jpg', 'Áo thun trắng mặt sau', false, NOW(), NOW()),
(2, 'https://example.com/product2-1.jpg', 'Áo sơ mi kẻ caro', true, NOW(), NOW()),
(3, 'https://example.com/product3-1.jpg', 'Quần jeans slimfit', true, NOW(), NOW()),
(4, 'https://example.com/product4-1.jpg', 'Váy maxi hoa tím', true, NOW(), NOW());


-- CART
INSERT INTO carts (user_id, created_at, updated_at) VALUES
(2, NOW(), NOW()), -- cho user Nguyễn Văn A
(3, NOW(), NOW()), -- cho user Trần Thị B
(4, NOW(), NOW()); -- cho user Lê Văn C

-- CART_ITEM
INSERT INTO cart_items (cart_id, variant_id, quantity, unit_price, total_price, created_at, updated_at) VALUES
(1, 9, 2, 199000.00, 398000.00, NOW(), NOW()),   -- user A: 2 áo thun M trắng (id=9)
(1, 12, 1, 450000.00, 450000.00, NOW(), NOW()),  -- user A: 1 áo sơ mi M xanh kẻ (id=12)
(2, 9, 1, 199000.00, 169150.00, NOW(), NOW());   -- user B: 1 áo thun M trắng

-- REVIEW
INSERT INTO reviews (product_id, user_id, rating, comment, image_url, is_approved, created_at, updated_at) VALUES
(1, 2, 5, 'Áo đẹp, chất liệu tốt!', NULL, true, NOW(), NOW()),
(1, 3, 4, 'Mặc thoải mái, giao hàng nhanh', 'https://example.com/review1.jpg', true, NOW(), NOW()),
(2, 4, 5, 'Rất lịch sự, phù hợp công sở', NULL, true, NOW(), NOW());

-- ORDER
INSERT INTO orders (user_id, total_amount, payment_method, order_status, created_at, updated_at) 
VALUES (2, 848000.00, 'COD', 'DELIVERED', NOW(), NOW());

-- ORDER _ITEM
INSERT INTO order_items (order_id, variant_id, quantity, unit_price, total_price, created_at, updated_at) VALUES
(1, 9, 2, 199000.00, 398000.00, NOW(), NOW()),   -- 2 áo thun size M trắng (id=9)
(1, 12, 1, 450000.00, 450000.00, NOW(), NOW());  -- 1 áo sơ mi size M xanh kẻ (id=12)
-- ADDRESS
INSERT INTO order_addresses (order_id, recipient_name, phone, address_line, ward, district, city, note, created_at, updated_at) VALUES
(1, 'Nguyễn Văn A', '0912345678', '123 Đường Láng', 'Phường Láng Thượng', 'Quận Đống Đa', 'Hà Nội', 'Giao buổi sáng', NOW(), NOW());
-- PAYMENTS
INSERT INTO payments (order_id, method, status, amount, paid_at, created_at, updated_at) VALUES
(1, 'COD', 'PENDING', 848000.00, NULL, NOW(), NOW());