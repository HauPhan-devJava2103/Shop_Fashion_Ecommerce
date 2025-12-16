-- =====================================================
-- FASHION SHOP - SAMPLE DATA (CHUẨN THEO ENTITY 100%)
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

-- XÓA DỮ LIỆU CŨ
TRUNCATE TABLE payment_transactions;
TRUNCATE TABLE payments;
TRUNCATE TABLE reviews;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE carts;
TRUNCATE TABLE order_addresses;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE product_variants;
TRUNCATE TABLE images;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE vouchers;
TRUNCATE TABLE role_permissions;
TRUNCATE TABLE permissions;
TRUNCATE TABLE users;
TRUNCATE TABLE roles;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1. ROLES
-- Entity: role_name (Enum), description, is_active
-- =====================================================
INSERT INTO roles (role_name, description, is_active, created_at, updated_at) VALUES
('ADMIN', 'Quản trị viên hệ thống', TRUE, NOW(), NOW()),
('CUSTOMER', 'Khách hàng', TRUE, NOW(), NOW()),
('STAFF', 'Nhân viên bán hàng', TRUE, NOW(), NOW());

-- =====================================================
-- 2. USERS
-- Entity: full_name, email, phone, gender (Enum), address, password, role_id
-- =====================================================
INSERT INTO users (full_name, email, phone, gender, address, password, role_id, is_active, created_at, updated_at) VALUES
('Nguyễn Văn Admin', 'admin@fashionshop.com', '0901234567', 'MALE', '123 Nguyễn Huệ, Q1, TP.HCM', '123456', 1, TRUE, NOW(), NOW()),
('Trần Thị Staff', 'staff@fashionshop.com', '0902345678', 'FEMALE', '456 Lê Lợi, Q1, TP.HCM', '123456', 3, TRUE, NOW(), NOW()),
('Lê Minh Tuấn', 'tuanle@gmail.com', '0903456789', 'MALE', '789 Võ Văn Tần, Q3, TP.HCM', '123456', 2, TRUE, NOW(), NOW()),
('Phạm Thu Hương', 'huongpham@gmail.com', '0904567890', 'FEMALE', '321 Điện Biên Phủ, Q10, TP.HCM', '123456', 2, TRUE, NOW(), NOW()),
('Hoàng Văn Nam', 'namhoang@gmail.com', '0905678901', 'MALE', '654 CMT8, Q3, TP.HCM', '123456', 2, TRUE, NOW(), NOW()),
-- Test users for Chart (7 days)
('Nguyễn Test 1', 'test1@gmail.com', '0911111111', 'MALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 0 DAY), NOW()),
('Trần Test 2', 'test2@gmail.com', '0911111112', 'FEMALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 0 DAY), NOW()),
('Lê Test 3', 'test3@gmail.com', '0911111113', 'MALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('Phạm Test 4', 'test4@gmail.com', '0911111114', 'FEMALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('Hoàng Test 5', 'test5@gmail.com', '0911111115', 'MALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('Vũ Test 6', 'test6@gmail.com', '0911111116', 'FEMALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('Đặng Test 7', 'test7@gmail.com', '0911111117', 'MALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('Bùi Test 8', 'test8@gmail.com', '0911111118', 'FEMALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
('Đỗ Test 9', 'test9@gmail.com', '0911111119', 'MALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
('Hồ Test 10', 'test10@gmail.com', '0911111120', 'FEMALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
('Ngô Test 11', 'test11@gmail.com', '0911111121', 'MALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('Dương Test 12', 'test12@gmail.com', '0911111122', 'FEMALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('Lý Test 13', 'test13@gmail.com', '0911111123', 'MALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('Võ Test 14', 'test14@gmail.com', '0911111124', 'FEMALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),
('Phan Test 15', 'test15@gmail.com', '0911111125', 'MALE', 'HCM', '123456', 2, TRUE, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW());

-- =====================================================
-- 3. CATEGORIES
-- Entity: category_name, slug, image_url, parent_id
-- =====================================================
INSERT INTO categories (category_name, slug, image_url, is_active, created_at, updated_at) VALUES
('Áo Nam', 'ao-nam', '/images/categories/ao-nam.jpg', TRUE, NOW(), NOW()),
('Áo Nữ', 'ao-nu', '/images/categories/ao-nu.jpg', TRUE, NOW(), NOW()),
('Quần Nam', 'quan-nam', '/images/categories/quan-nam.jpg', TRUE, NOW(), NOW()),
('Quần Nữ', 'quan-nu', '/images/categories/quan-nu.jpg', TRUE, NOW(), NOW()),
('Váy Đầm', 'vay-dam', '/images/categories/vay-dam.jpg', TRUE, NOW(), NOW()),
('Phụ Kiện', 'phu-kien', '/images/categories/phu-kien.jpg', TRUE, NOW(), NOW());

-- =====================================================
-- 4. PRODUCTS
-- Entity: sku, product_name, description, stock, price, discount, category_id
-- =====================================================
INSERT INTO products (sku, product_name, description, stock, price, discount, category_id, is_active, created_at, updated_at) VALUES
('SKU-001', 'Áo Thun Nam Basic', 'Áo thun cotton 100%', 100, 150000.00, 20.00, 1, TRUE, NOW(), NOW()),
('SKU-002', 'Áo Sơ Mi Nam', 'Áo sơ mi công sở', 80, 350000.00, 10.00, 1, TRUE, NOW(), NOW()),
('SKU-003', 'Áo Khoác Bomber', 'Áo khoác dù 2 lớp', 60, 550000.00, 0.00, 1, TRUE, NOW(), NOW()),
('SKU-004', 'Áo Thun Nữ Oversize', 'Áo thun form rộng', 150, 180000.00, 15.00, 2, TRUE, NOW(), NOW()),
('SKU-005', 'Quần Jean Nam Slim', 'Quần jean co giãn', 100, 450000.00, 0.00, 3, TRUE, NOW(), NOW()),
('SKU-006', 'Váy Maxi Hoa', 'Váy maxi voan lụa', 60, 420000.00, 10.00, 5, TRUE, NOW(), NOW());

-- =====================================================
-- 5. PRODUCT VARIANTS
-- Entity: product_id, size (Enum), color, stock, sku_variant
-- =====================================================
INSERT INTO product_variants (product_id, size, color, stock, sku_variant, created_at, updated_at) VALUES
(1, 'M', 'Trắng', 30, 'SKU-001-M-WHITE', NOW(), NOW()),
(1, 'L', 'Trắng', 40, 'SKU-001-L-WHITE', NOW(), NOW()),
(1, 'M', 'Đen', 30, 'SKU-001-M-BLACK', NOW(), NOW()),
(2, 'L', 'Xanh', 30, 'SKU-002-L-BLUE', NOW(), NOW()),
(2, 'XL', 'Xanh', 20, 'SKU-002-XL-BLUE', NOW(), NOW()),
(5, 'S', 'Xanh Đậm', 30, 'SKU-005-S-DBLUE', NOW(), NOW()),
(5, 'M', 'Xanh Đậm', 40, 'SKU-005-M-DBLUE', NOW(), NOW()),
(6, 'M', 'Đỏ', 20, 'SKU-006-M-RED', NOW(), NOW()),
(6, 'L', 'Vàng', 20, 'SKU-006-L-YELLOW', NOW(), NOW());

-- =====================================================
-- 6. IMAGES
-- Entity: product_id, url_image, alt_text, is_main
-- =====================================================
INSERT INTO images (product_id, url_image, alt_text, is_main, created_at, updated_at) VALUES
(1, '/images/products/ao-thun-nam-1.jpg', 'Áo thun nam trắng', TRUE, NOW(), NOW()),
(1, '/images/products/ao-thun-nam-2.jpg', 'Áo thun nam đen', FALSE, NOW(), NOW()),
(2, '/images/products/ao-somi-1.jpg', 'Áo sơ mi xanh', TRUE, NOW(), NOW()),
(5, '/images/products/quan-jean-1.jpg', 'Quần jean slim', TRUE, NOW(), NOW()),
(6, '/images/products/vay-maxi-1.jpg', 'Váy maxi hoa', TRUE, NOW(), NOW());

-- =====================================================
-- 7. VOUCHERS
-- Entity: code, description, discount_percent, max_discount_amount, min_order_value
-- =====================================================
INSERT INTO vouchers (code, description, discount_percent, max_discount_amount, min_order_value, start_at, end_at, usage_limit, used_count, is_active, created_at, updated_at) VALUES
('WELCOME10', 'Giảm 10% đơn đầu tiên', 10, 50000.00, 200000.00, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 1000, 5, TRUE, NOW(), NOW()),
('SUMMER20', 'Sale hè 20%', 20, 100000.00, 500000.00, NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 500, 20, TRUE, NOW(), NOW()),
('FREESHIP', 'Free ship', 5, 30000.00, 100000.00, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, 50, TRUE, NOW(), NOW());

-- =====================================================
-- 8. ORDERS
-- Entity: user_id, sub_total, discount_amount, total_amount, voucher_id, voucher_code, voucher_discount_percent, payment_method (Enum), order_status (Enum)
-- =====================================================
INSERT INTO orders (user_id, sub_total, discount_amount, total_amount, voucher_id, voucher_code, voucher_discount_percent, payment_method, order_status, created_at, updated_at) VALUES
(3, 450000.00, 45000.00, 405000.00, 1, 'WELCOME10', 10, 'COD', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
(4, 550000.00, 0.00, 550000.00, NULL, NULL, NULL, 'BANK_TRANSFER', 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(3, 800000.00, 100000.00, 700000.00, 2, 'SUMMER20', 20, 'COD', 'PENDING', NOW(), NOW());

-- =====================================================
-- 9. ORDER ITEMS
-- Entity: order_id, variant_id, quantity, unit_price, total_price
-- =====================================================
INSERT INTO order_items (order_id, variant_id, quantity, unit_price, total_price, created_at, updated_at) VALUES
(1, 1, 1, 120000.00, 120000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
(1, 6, 1, 330000.00, 330000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
(2, 4, 1, 315000.00, 315000.00, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(2, 5, 1, 315000.00, 315000.00, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(3, 8, 1, 378000.00, 378000.00, NOW(), NOW()),
(3, 9, 1, 378000.00, 378000.00, NOW(), NOW());

-- =====================================================
-- 10. ORDER ADDRESSES
-- Entity: order_id, recipient_name, phone, address_line, ward, district, city
-- =====================================================
INSERT INTO order_addresses (order_id, recipient_name, phone, address_line, ward, district, city, created_at, updated_at) VALUES
(1, 'Lê Minh Tuấn', '0903456789', '789 Võ Văn Tần', 'Phường 6', 'Quận 3', 'TP.HCM', NOW(), NOW()),
(2, 'Phạm Thu Hương', '0904567890', '321 Điện Biên Phủ', 'Phường 17', 'Quận Bình Thạnh', 'TP.HCM', NOW(), NOW()),
(3, 'Lê Minh Tuấn', '0903456789', '789 Võ Văn Tần', 'Phường 6', 'Quận 3', 'TP.HCM', NOW(), NOW());

-- =====================================================
-- 11. PAYMENTS
-- Entity: order_id, method (Enum), status (Enum), amount, paid_at
-- =====================================================
INSERT INTO payments (order_id, method, status, amount, paid_at, created_at, updated_at) VALUES
(1, 'COD', 'SUCCESS', 405000.00, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW(), NOW()),
(2, 'BANK_TRANSFER', 'SUCCESS', 550000.00, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), NOW()),
(3, 'COD', 'PENDING', 700000.00, NULL, NOW(), NOW());

-- =====================================================
-- 12. PAYMENT TRANSACTIONS
-- Entity: payment_id, gateway (Enum), txn_ref, gateway_txn_id, response_message
-- =====================================================
INSERT INTO payment_transactions (payment_id, gateway, txn_ref, gateway_txn_id, response_message, created_at, updated_at) VALUES
(2, 'VNPAY', 'TXN202412160001', 'VNP123456789', 'Giao dịch thành công', NOW(), NOW());

-- =====================================================
-- 13. CARTS
-- Entity: user_id
-- =====================================================
INSERT INTO carts (user_id, created_at, updated_at) VALUES
(3, NOW(), NOW()),
(4, NOW(), NOW()),
(5, NOW(), NOW());

-- =====================================================
-- 14. CART ITEMS
-- Entity: cart_id, variant_id, quantity, unit_price, total_price
-- =====================================================
INSERT INTO cart_items (cart_id, variant_id, quantity, unit_price, total_price, created_at, updated_at) VALUES
(1, 2, 2, 120000.00, 240000.00, NOW(), NOW()),
(2, 8, 1, 378000.00, 378000.00, NOW(), NOW()),
(3, 3, 1, 120000.00, 120000.00, NOW(), NOW());

-- =====================================================
-- 15. PERMISSIONS
-- Entity: permission_name, api_path, method, module (Enum)
-- =====================================================
INSERT INTO permissions (permission_name, description, api_path, method, module, is_active, created_at, updated_at) VALUES
('USER_READ', 'Xem user', '/api/users', 'GET', 'USER', TRUE, NOW(), NOW()),
('PRODUCT_READ', 'Xem sản phẩm', '/api/products', 'GET', 'PRODUCT', TRUE, NOW(), NOW()),
('ORDER_MANAGE', 'Quản lý đơn hàng', '/api/orders', 'POST', 'ORDER', TRUE, NOW(), NOW());

-- =====================================================
-- 16. ROLE PERMISSIONS
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3),
(3, 1), (3, 2);

-- =====================================================
-- 17. REVIEWS
-- Entity: order_item_id, product_id, user_id, rating, comment, is_approved
-- =====================================================
INSERT INTO reviews (order_item_id, product_id, user_id, rating, comment, is_approved, created_at, updated_at) VALUES
(1, 1, 3, 5, 'Áo thun rất đẹp, chất lượng tốt!', TRUE, NOW(), NOW()),
(2, 5, 3, 4, 'Quần jean vừa vặn, giao hàng nhanh', TRUE, NOW(), NOW());

-- =====================================================
SELECT '✅ Data import completed successfully!' AS Status;