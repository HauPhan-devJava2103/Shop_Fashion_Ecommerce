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
-- MySQL/InnoDB does not allow TRUNCATE on a table referenced by a foreign key
-- (even if the referencing table is empty). Use DELETE + reset identity instead.
-- Also detach vouchers from orders first so this block works even when executed
-- separately in tools like MySQL Workbench.
UPDATE orders
SET voucher_id = NULL,
	voucher_code = NULL,
	voucher_discount_percent = NULL
WHERE voucher_id IS NOT NULL;

DELETE FROM vouchers WHERE id > 0;
ALTER TABLE vouchers AUTO_INCREMENT = 1;
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
('Nguyễn Văn Admin', 'admin@fashionshop.com', '0901234567', 'MALE', '123 Nguyễn Huệ, Q1, TP.HCM', '123456', (SELECT id FROM roles WHERE role_name = 'ADMIN'), TRUE, NOW(), NOW()),
('Trần Thị Staff', 'staff@fashionshop.com', '0902345678', 'FEMALE', '456 Lê Lợi, Q1, TP.HCM', '123456', (SELECT id FROM roles WHERE role_name = 'STAFF'), TRUE, NOW(), NOW()),
('Lê Minh Tuấn', 'tuanle@gmail.com', '0903456789', 'MALE', '789 Võ Văn Tần, Q3, TP.HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, NOW(), NOW()),
('Phạm Thu Hương', 'huongpham@gmail.com', '0904567890', 'FEMALE', '321 Điện Biên Phủ, Q10, TP.HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, NOW(), NOW()),
('Hoàng Văn Nam', 'namhoang@gmail.com', '0905678901', 'MALE', '654 CMT8, Q3, TP.HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, NOW(), NOW()),
-- Test users for Chart (7 days)
('Nguyễn Test 1', 'test1@gmail.com', '0911111111', 'MALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 0 DAY), NOW()),
('Trần Test 2', 'test2@gmail.com', '0911111112', 'FEMALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 0 DAY), NOW()),
('Lê Test 3', 'test3@gmail.com', '0911111113', 'MALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('Phạm Test 4', 'test4@gmail.com', '0911111114', 'FEMALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('Hoàng Test 5', 'test5@gmail.com', '0911111115', 'MALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('Vũ Test 6', 'test6@gmail.com', '0911111116', 'FEMALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('Đặng Test 7', 'test7@gmail.com', '0911111117', 'MALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('Bùi Test 8', 'test8@gmail.com', '0911111118', 'FEMALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
('Đỗ Test 9', 'test9@gmail.com', '0911111119', 'MALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
('Hồ Test 10', 'test10@gmail.com', '0911111120', 'FEMALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
('Ngô Test 11', 'test11@gmail.com', '0911111121', 'MALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('Dương Test 12', 'test12@gmail.com', '0911111122', 'FEMALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('Lý Test 13', 'test13@gmail.com', '0911111123', 'MALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('Võ Test 14', 'test14@gmail.com', '0911111124', 'FEMALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),
('Phan Test 15', 'test15@gmail.com', '0911111125', 'MALE', 'HCM', '123456', (SELECT id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW());

-- =====================================================
-- 3. CATEGORIES
-- Entity: category_name, slug, image_url, parent_id
-- =====================================================
-- Root categories (phục vụ /collections/{rootSlug})
INSERT INTO categories (category_name, parent_id, slug, image_url, is_active, created_at, updated_at) VALUES
('Thời trang nam', NULL, 'men', NULL, TRUE, NOW(), NOW()),
('Thời trang nữ', NULL, 'women', NULL, TRUE, NOW(), NOW()),
('Phụ kiện', NULL, 'accessories', NULL, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
	parent_id = VALUES(parent_id),
	slug = VALUES(slug),
	image_url = VALUES(image_url),
	is_active = VALUES(is_active),
	updated_at = VALUES(updated_at);

SET @men_id := (SELECT id FROM categories WHERE slug = 'men' LIMIT 1);
SET @women_id := (SELECT id FROM categories WHERE slug = 'women' LIMIT 1);
SET @accessories_id := (SELECT id FROM categories WHERE slug = 'accessories' LIMIT 1);

-- Child categories (phục vụ lọc theo menu mega-menu)
INSERT INTO categories (category_name, parent_id, slug, image_url, is_active, created_at, updated_at) VALUES
('Áo polo', @men_id, 'men-ao-polo', NULL, TRUE, NOW(), NOW()),
('Áo blazer', @men_id, 'men-ao-blazer', NULL, TRUE, NOW(), NOW()),
('Áo sơ mi', @men_id, 'men-ao-so-mi', NULL, TRUE, NOW(), NOW()),
('Quần jean', @men_id, 'men-quan-jean', NULL, TRUE, NOW(), NOW()),
('Quần tây', @men_id, 'men-quan-tay', NULL, TRUE, NOW(), NOW()),
('Đầm', @women_id, 'women-dam', NULL, TRUE, NOW(), NOW()),
('Áo', @women_id, 'women-ao', NULL, TRUE, NOW(), NOW()),
('Quần', @women_id, 'women-quan', NULL, TRUE, NOW(), NOW()),
('Chân váy', @women_id, 'women-chan-vay', NULL, TRUE, NOW(), NOW()),
('Áo khoác', @women_id, 'women-ao-khoac', NULL, TRUE, NOW(), NOW()),
('Tất', @accessories_id, 'accessories-tat', NULL, TRUE, NOW(), NOW()),
('Ví da', @accessories_id, 'accessories-vi-da', NULL, TRUE, NOW(), NOW()),
('Cà vạt', @accessories_id, 'accessories-ca-vat', NULL, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
	parent_id = VALUES(parent_id),
	slug = VALUES(slug),
	image_url = VALUES(image_url),
	is_active = VALUES(is_active),
	updated_at = VALUES(updated_at);

-- =====================================================
-- 4. PRODUCTS
-- Entity: sku, product_name, description, stock, price, discount, category_id
-- =====================================================
INSERT INTO products (sku, product_name, description, stock, price, discount, category_id, is_active, created_at, updated_at) VALUES
('SKU-001', 'Áo Thun Nam Basic', 'Áo thun cotton 100%', 100, 150000.00, 20.00, (SELECT id FROM categories WHERE slug = 'men-ao-polo'), TRUE, NOW(), NOW()),
('SKU-002', 'Áo Sơ Mi Nam', 'Áo sơ mi công sở', 80, 350000.00, 10.00, (SELECT id FROM categories WHERE slug = 'men-ao-so-mi'), TRUE, NOW(), NOW()),
('SKU-003', 'Áo Khoác Bomber', 'Áo khoác dù 2 lớp', 60, 550000.00, 0.00, (SELECT id FROM categories WHERE slug = 'men-ao-blazer'), TRUE, NOW(), NOW()),
('SKU-004', 'Áo Thun Nữ Oversize', 'Áo thun form rộng', 150, 180000.00, 15.00, (SELECT id FROM categories WHERE slug = 'women-ao'), TRUE, NOW(), NOW()),
('SKU-005', 'Quần Jean Nam Slim', 'Quần jean co giãn', 100, 450000.00, 0.00, (SELECT id FROM categories WHERE slug = 'men-quan-jean'), TRUE, NOW(), NOW()),
('SKU-006', 'Váy Maxi Hoa', 'Váy maxi voan lụa', 60, 420000.00, 10.00, (SELECT id FROM categories WHERE slug = 'women-dam'), TRUE, NOW(), NOW()),
-- New Blazer collection (seed)
('BL241362', 'Áo Blazer - BL241362', 'Blazer lịch lãm, form chuẩn, phù hợp công sở và sự kiện.', 80, 2150000.00, 30.00, (SELECT id FROM categories WHERE slug = 'men-ao-blazer'), TRUE, NOW(), NOW()),
('BL231492', 'Áo Blazer - BL231492', 'Blazer tối giản, dễ phối đồ, chất liệu đứng form.', 80, 2150000.00, 50.00, (SELECT id FROM categories WHERE slug = 'men-ao-blazer'), TRUE, NOW(), NOW()),
('BL220699', 'Blazer (Demi) - BL220699', 'Blazer demi trẻ trung, phù hợp đi làm và dạo phố.', 80, 1950000.00, 0.00, (SELECT id FROM categories WHERE slug = 'men-ao-blazer'), TRUE, NOW(), NOW()),
('BL231489', 'Áo Blazer - BL231489', 'Blazer sọc thanh lịch, tạo điểm nhấn tinh tế.', 80, 2150000.00, 30.00, (SELECT id FROM categories WHERE slug = 'men-ao-blazer'), TRUE, NOW(), NOW()),
-- Accessories: Socks (3 products)
('SOCK-001', 'Tất cổ ngắn basic - SOCK-001', 'Tất cotton thấm hút, mềm mại, phù hợp đi hằng ngày.', 120, 59000.00, 0.00, (SELECT id FROM categories WHERE slug = 'accessories-tat'), TRUE, NOW(), NOW()),
('SOCK-002', 'Tất thể thao - SOCK-002', 'Tất thể thao co giãn, thoáng khí, hạn chế mùi.', 100, 79000.00, 10.00, (SELECT id FROM categories WHERE slug = 'accessories-tat'), TRUE, NOW(), NOW()),
('SOCK-003', 'Tất dài kẻ sọc - SOCK-003', 'Tất dài phong cách, phối đồ streetwear.', 80, 99000.00, 0.00, (SELECT id FROM categories WHERE slug = 'accessories-tat'), TRUE, NOW(), NOW()),
-- Accessories: Wallets (3 products)
('WALLET-001', 'Ví da mini - WALLET-001', 'Ví da nhỏ gọn, nhiều ngăn, phù hợp mang theo hằng ngày.', 50, 299000.00, 0.00, (SELECT id FROM categories WHERE slug = 'accessories-vi-da'), TRUE, NOW(), NOW()),
('WALLET-002', 'Ví da đứng - WALLET-002', 'Ví da đứng, thiết kế tối giản, bền bỉ.', 40, 399000.00, 5.00, (SELECT id FROM categories WHERE slug = 'accessories-vi-da'), TRUE, NOW(), NOW()),
('WALLET-003', 'Ví da cao cấp - WALLET-003', 'Ví da cao cấp, đường may chắc chắn, sang trọng.', 30, 499000.00, 10.00, (SELECT id FROM categories WHERE slug = 'accessories-vi-da'), TRUE, NOW(), NOW()),
-- Accessories: Ties (3 products)
('TIE-001', 'Cà vạt trơn - TIE-001', 'Cà vạt trơn lịch lãm, dễ phối suit.', 70, 159000.00, 0.00, (SELECT id FROM categories WHERE slug = 'accessories-ca-vat'), TRUE, NOW(), NOW()),
('TIE-002', 'Cà vạt họa tiết - TIE-002', 'Cà vạt họa tiết tinh tế, phù hợp công sở.', 60, 199000.00, 0.00, (SELECT id FROM categories WHERE slug = 'accessories-ca-vat'), TRUE, NOW(), NOW()),
('TIE-003', 'Cà vạt bản nhỏ - TIE-003', 'Cà vạt bản nhỏ hiện đại, phong cách trẻ trung.', 50, 249000.00, 10.00, (SELECT id FROM categories WHERE slug = 'accessories-ca-vat'), TRUE, NOW(), NOW());

-- =====================================================
-- 5. PRODUCT VARIANTS
-- Entity: product_id, size (Enum), color, stock, sku_variant
-- =====================================================
INSERT INTO product_variants (product_id, size, color, stock, sku_variant, created_at, updated_at) VALUES
(1, 'M', 'Trắng', 30, 'SKU-001-M-WHITE', NOW(), NOW()),
(1, 'L', 'Trắng', 40, 'SKU-001-L-WHITE', NOW(), NOW()),
(1, 'M', 'Đen', 30, 'SKU-001-M-BLACK', NOW(), NOW()),
(2, 'L', 'Xanh', 40, 'SKU-002-L-BLUE', NOW(), NOW()),
(2, 'XL', 'Xanh', 40, 'SKU-002-XL-BLUE', NOW(), NOW()),
(3, 'L', 'Đen', 30, 'SKU-003-L-BLACK', NOW(), NOW()),
(3, 'XL', 'Đen', 30, 'SKU-003-XL-BLACK', NOW(), NOW()),
(4, 'M', 'Trắng', 75, 'SKU-004-M-WHITE', NOW(), NOW()),
(4, 'L', 'Trắng', 75, 'SKU-004-L-WHITE', NOW(), NOW()),
(5, 'S', 'Xanh Đậm', 50, 'SKU-005-S-DBLUE', NOW(), NOW()),
(5, 'M', 'Xanh Đậm', 50, 'SKU-005-M-DBLUE', NOW(), NOW()),
(6, 'M', 'Đỏ', 30, 'SKU-006-M-RED', NOW(), NOW()),
 (6, 'L', 'Vàng', 30, 'SKU-006-L-YELLOW', NOW(), NOW()),
 -- Variants for BL241362
 ((SELECT id FROM products WHERE sku = 'BL241362' LIMIT 1), 'M', 'Xanh Navy', 25, 'BL241362-M-NAVY', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'BL241362' LIMIT 1), 'L', 'Xanh Navy', 25, 'BL241362-L-NAVY', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'BL241362' LIMIT 1), 'XL', 'Xanh Navy', 30, 'BL241362-XL-NAVY', NOW(), NOW()),
 -- Variants for BL231492
 ((SELECT id FROM products WHERE sku = 'BL231492' LIMIT 1), 'M', 'Đen', 25, 'BL231492-M-BLACK', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'BL231492' LIMIT 1), 'L', 'Đen', 25, 'BL231492-L-BLACK', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'BL231492' LIMIT 1), 'XL', 'Đen', 30, 'BL231492-XL-BLACK', NOW(), NOW()),
 -- Variants for BL220699
 ((SELECT id FROM products WHERE sku = 'BL220699' LIMIT 1), 'M', 'Xám', 25, 'BL220699-M-GREY', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'BL220699' LIMIT 1), 'L', 'Xám', 25, 'BL220699-L-GREY', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'BL220699' LIMIT 1), 'XL', 'Xám', 30, 'BL220699-XL-GREY', NOW(), NOW()),
 -- Variants for BL231489
 ((SELECT id FROM products WHERE sku = 'BL231489' LIMIT 1), 'M', 'Trắng Sọc', 25, 'BL231489-M-STRIPE', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'BL231489' LIMIT 1), 'L', 'Trắng Sọc', 25, 'BL231489-L-STRIPE', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'BL231489' LIMIT 1), 'XL', 'Trắng Sọc', 30, 'BL231489-XL-STRIPE', NOW(), NOW()),
 -- Variants for accessories (minimal)
 ((SELECT id FROM products WHERE sku = 'SOCK-001' LIMIT 1), 'M', 'Trắng', 120, 'SOCK-001-M-WHITE', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'SOCK-002' LIMIT 1), 'M', 'Đen', 100, 'SOCK-002-M-BLACK', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'SOCK-003' LIMIT 1), 'M', 'Xám', 80, 'SOCK-003-M-GREY', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'WALLET-001' LIMIT 1), 'M', 'Nâu', 50, 'WALLET-001-M-BROWN', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'WALLET-002' LIMIT 1), 'M', 'Đen', 40, 'WALLET-002-M-BLACK', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'WALLET-003' LIMIT 1), 'M', 'Nâu Đậm', 30, 'WALLET-003-M-DKBR', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'TIE-001' LIMIT 1), 'M', 'Xanh Navy', 70, 'TIE-001-M-NAVY', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'TIE-002' LIMIT 1), 'M', 'Đỏ Đô', 60, 'TIE-002-M-MAROON', NOW(), NOW()),
 ((SELECT id FROM products WHERE sku = 'TIE-003' LIMIT 1), 'M', 'Đen', 50, 'TIE-003-M-BLACK', NOW(), NOW());

-- =====================================================
-- 6. IMAGES
-- Entity: product_id, url_image, alt_text, is_main
-- =====================================================
-- NOTE: dùng lookup theo SKU để tránh lệch ID khi chạy seed từng phần
SET @OLD_SQL_SAFE_UPDATES := IFNULL(@@SQL_SAFE_UPDATES, 0);
SET SQL_SAFE_UPDATES = 0;

DELETE FROM images
WHERE product_id IN (SELECT id FROM products WHERE sku IN ('SKU-001', 'SKU-002', 'SKU-005', 'SKU-006', 'BL241362', 'BL231492', 'BL220699', 'BL231489'));

INSERT INTO images (product_id, url_image, alt_text, is_main, created_at, updated_at) VALUES
((SELECT id FROM products WHERE sku = 'SKU-001' LIMIT 1), '/img/product-img/product-1.jpg', 'Áo thun nam', TRUE, NOW(), NOW()),
((SELECT id FROM products WHERE sku = 'SKU-001' LIMIT 1), '/img/product-img/product-2.jpg', 'Áo thun nam', FALSE, NOW(), NOW()),
((SELECT id FROM products WHERE sku = 'SKU-002' LIMIT 1), '/img/product-img/product-3.jpg', 'Áo sơ mi nam', TRUE, NOW(), NOW()),
((SELECT id FROM products WHERE sku = 'SKU-005' LIMIT 1), '/img/product-img/product-4.jpg', 'Quần jean nam', TRUE, NOW(), NOW()),
((SELECT id FROM products WHERE sku = 'SKU-006' LIMIT 1), '/img/product-img/product-5.jpg', 'Váy nữ', TRUE, NOW(), NOW()),
-- Blazer images (placeholders from existing static assets)
((SELECT id FROM products WHERE sku = 'BL241362' LIMIT 1), '/img/product-img/blazer-bl241362.jpg', 'Áo blazer BL241362', TRUE, NOW(), NOW()),
((SELECT id FROM products WHERE sku = 'BL231492' LIMIT 1), '/img/product-img/blazer-bl231492.jpg', 'Áo blazer BL231492', TRUE, NOW(), NOW()),
((SELECT id FROM products WHERE sku = 'BL220699' LIMIT 1), '/img/product-img/blazer-bl220699.jpg', 'Blazer demi BL220699', TRUE, NOW(), NOW()),
((SELECT id FROM products WHERE sku = 'BL231489' LIMIT 1), '/img/product-img/blazer-bl231489.jpg', 'Áo blazer BL231489', TRUE, NOW(), NOW());

SET SQL_SAFE_UPDATES = IFNULL(@OLD_SQL_SAFE_UPDATES, 0);

-- =====================================================
-- 7. VOUCHERS
-- Entity: code, description, discount_percent, max_discount_amount, min_order_value
-- =====================================================
INSERT INTO vouchers (code, description, discount_percent, max_discount_amount, min_order_value, start_at, end_at, usage_limit, used_count, is_active, created_at, updated_at) VALUES
('WELCOME10', 'Giảm 10% đơn đầu tiên', 10, 50000.00, 200000.00, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 1000, 5, TRUE, NOW(), NOW()),
('SUMMER20', 'Sale hè 20%', 20, 100000.00, 500000.00, NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 500, 20, TRUE, NOW(), NOW()),
('FREESHIP', 'Free ship', 5, 30000.00, 100000.00, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, 50, TRUE, NOW(), NOW()),
('VIP30', 'VIP giảm 30% (tối đa 150k)', 30, 150000.00, 700000.00, NOW(), DATE_ADD(NOW(), INTERVAL 6 MONTH), 300, 0, TRUE, NOW(), NOW()),
('SALE50K', 'Giảm 15% (tối đa 50k)', 15, 50000.00, 300000.00, NOW(), DATE_ADD(NOW(), INTERVAL 6 MONTH), 800, 0, TRUE, NOW(), NOW());

-- =====================================================
-- 8. ORDERS
-- Entity: user_id, sub_total, discount_amount, total_amount, voucher_id, voucher_code, voucher_discount_percent, payment_method (Enum), order_status (Enum)
-- =====================================================
-- LOGIC: 
--   sub_total = SUM(order_items.total_price) 
--   discount_amount = sub_total * voucher_discount_percent / 100
--   total_amount = sub_total - discount_amount
INSERT INTO orders (user_id, sub_total, discount_amount, total_amount, voucher_id, voucher_code, voucher_discount_percent, payment_method, order_status, created_at, updated_at) VALUES
-- Order 1: (150k -20% = 120k) + (350k -10% = 315k) => 435k, voucher 10% => -43.5k, total 391.5k
(3, 435000.00, 43500.00, 391500.00, 1, 'WELCOME10', 10, 'COD', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
-- Order 2: 1 áo khoác (550k -0% = 550k), no voucher
(4, 550000.00, 0.00, 550000.00, NULL, NULL, NULL, 'BANK_TRANSFER', 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
-- Order 3: (378k + 378k) = 756k, voucher 20% = -151.2k, total = 604.8k ✅ (FIXED!)
(3, 756000.00, 151200.00, 604800.00, 2, 'SUMMER20', 20, 'COD', 'PENDING', NOW(), NOW());

-- =====================================================
-- 9. ORDER ITEMS
-- Entity: order_id, variant_id, quantity, unit_price, total_price
-- =====================================================
-- LOGIC:
--   unit_price = product.price - (product.price * product.discount / 100) [Giá tại thời điểm đặt]
--   total_price = unit_price * quantity
INSERT INTO order_items (order_id, variant_id, quantity, unit_price, total_price, created_at, updated_at) VALUES
-- Order 1: Áo thun (150k - 20% = 120k) + Sơ mi (350k - 10% = 315k)
(1, (SELECT id FROM product_variants WHERE sku_variant = 'SKU-001-M-WHITE'), 1, 120000.00, 120000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
(1, (SELECT id FROM product_variants WHERE sku_variant = 'SKU-002-L-BLUE'), 1, 315000.00, 315000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
-- Order 2: Áo khoác (550k - 0% = 550k)
(2, (SELECT id FROM product_variants WHERE sku_variant = 'SKU-003-L-BLACK'), 1, 550000.00, 550000.00, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
-- Order 3: 2 váy maxi (420k - 10% = 378k mỗi chiếc)
(3, (SELECT id FROM product_variants WHERE sku_variant = 'SKU-006-M-RED'), 1, 378000.00, 378000.00, NOW(), NOW()),
(3, (SELECT id FROM product_variants WHERE sku_variant = 'SKU-006-L-YELLOW'), 1, 378000.00, 378000.00, NOW(), NOW());

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
(1, 'COD', 'SUCCESS', 391500.00, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW(), NOW()),
(2, 'BANK_TRANSFER', 'SUCCESS', 550000.00, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), NOW()),
(3, 'COD', 'PENDING', 604800.00, NULL, NOW(), NOW());  -- Updated to match Order.total_amount

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
(1, (SELECT id FROM product_variants WHERE sku_variant = 'SKU-001-L-WHITE'), 2, 120000.00, 240000.00, NOW(), NOW()),
(2, (SELECT id FROM product_variants WHERE sku_variant = 'SKU-006-M-RED'), 1, 378000.00, 378000.00, NOW(), NOW()),
(3, (SELECT id FROM product_variants WHERE sku_variant = 'SKU-001-M-BLACK'), 1, 120000.00, 120000.00, NOW(), NOW());

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
(2, 2, 3, 4, 'Áo sơ mi vừa vặn, giao hàng nhanh', TRUE, NOW(), NOW());

-- =====================================================
SELECT '✅ Data import completed successfully!' AS Status;