-- Sample Categories for Testing
-- Run this SQL to populate categories table

-- Root Categories (no parent)
INSERT INTO categories (category_name, parent_id, slug, image_url, is_active, created_at, updated_at) VALUES
('Men', NULL, 'men', NULL, true, NOW(), NOW()),
('Women', NULL, 'women', NULL, true, NOW(), NOW()),
('Kids', NULL, 'kids', NULL, true, NOW(), NOW()),
('Accessories', NULL, 'accessories', NULL, true, NOW(), NOW());

-- Child Categories for Men
INSERT INTO categories (category_name, parent_id, slug, image_url, is_active, created_at, updated_at) VALUES
('Áo thun nam', (SELECT id FROM categories WHERE slug = 'men'), 'ao-thun-nam', NULL, true, NOW(), NOW()),
('Quần jean nam', (SELECT id FROM categories WHERE slug = 'men'), 'quan-jean-nam', NULL, true, NOW(), NOW()),
('Áo khoác nam', (SELECT id FROM categories WHERE slug = 'men'), 'ao-khoac-nam', NULL, true, NOW(), NOW());

-- Child Categories for Women
INSERT INTO categories (category_name, parent_id, slug, image_url, is_active, created_at, updated_at) VALUES
('Váy', (SELECT id FROM categories WHERE slug = 'women'), 'vay', NULL, true, NOW(), NOW()),
('Áo kiểu', (SELECT id FROM categories WHERE slug = 'women'), 'ao-kieu', NULL, true, NOW(), NOW()),
('Quần jean nữ', (SELECT id FROM categories WHERE slug = 'women'), 'quan-jean-nu', NULL, true, NOW(), NOW());

-- Child Categories for Kids
INSERT INTO categories (category_name, parent_id, slug, image_url, is_active, created_at, updated_at) VALUES
('Quần áo bé trai', (SELECT id FROM categories WHERE slug = 'kids'), 'quan-ao-be-trai', NULL, true, NOW(), NOW()),
('Quần áo bé gái', (SELECT id FROM categories WHERE slug = 'kids'), 'quan-ao-be-gai', NULL, true, NOW(), NOW());

-- Child Categories for Accessories
INSERT INTO categories (category_name, parent_id, slug, image_url, is_active, created_at, updated_at) VALUES
('Túi xách', (SELECT id FROM categories WHERE slug = 'accessories'), 'tui-xach', NULL, true, NOW(), NOW()),
('Mũ/Nón', (SELECT id FROM categories WHERE slug = 'accessories'), 'mu-non', NULL, true, NOW(), NOW()),
('Ví', (SELECT id FROM categories WHERE slug = 'accessories'), 'vi', NULL, true, NOW(), NOW());
