-- 1. Insert Roles & Users
INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', 'Admin'), ('ROLE_USER', 'Customer');

INSERT INTO users (username, password, email, full_name, is_active) VALUES 
('admin', '$2a$10$gqHrSlMtgQwd0qqHHdr5.O.U.N.U.k.U.U.U.U.U.U.U.U.U.U.U', 'admin@hexamarket.com', 'System Admin', true),
('hieuvm', '$2a$10$gqHrSlMtgQwd0qqHHdr5.O.U.N.U.k.U.U.U.U.U.U.U.U.U.U.U', 'user@hexamarket.com', 'Nguyen Van Hieu', true);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1), (2, 2);

-- 2. Insert Categories
INSERT INTO categories (name, slug, description) VALUES ('Gaming Gear', 'gaming-gear', 'Phụ kiện game');
INSERT INTO categories (name, slug, description, parent_id) VALUES ('Chuột Gaming', 'gaming-mouse', 'Chuột FPS/MOBA', 1);
INSERT INTO categories (name, slug, description, parent_id) VALUES ('Bàn Phím', 'keyboard', 'Bàn phím cơ', 1);

-- 3. Insert Products (10 sản phẩm - Mỗi sản phẩm 1 biến thể đại diện cho đơn giản)

-- SP 1
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Logitech G102 Lightsync', 'logitech-g102', 450000, 'https://placehold.co/600x400/png?text=Logitech+G102', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='logitech-g102'), 'G102-BLK', 450000, '{"color": "Black", "dpi": 8000}', 'https://placehold.co/600x400/png?text=G102+Black');

-- SP 2
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Razer DeathAdder Essential', 'razer-da', 690000, 'https://placehold.co/600x400/png?text=Razer+DeathAdder', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='razer-da'), 'RZ-DA-BLK', 690000, '{"color": "Black", "dpi": 6400}', 'https://placehold.co/600x400/png?text=DeathAdder+Black');

-- SP 3
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Corsair Harpoon RGB', 'corsair-harpoon', 390000, 'https://placehold.co/600x400/png?text=Corsair+Harpoon', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='corsair-harpoon'), 'COR-HAR-WIRE', 390000, '{"conn": "Wired", "dpi": 6000}', 'https://placehold.co/600x400/png?text=Harpoon+Wired');

-- SP 4
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('SteelSeries Rival 3', 'rival-3', 750000, 'https://placehold.co/600x400/png?text=SteelSeries+Rival+3', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='rival-3'), 'SS-R3', 750000, '{"color": "Black", "led": "Prism"}', 'https://placehold.co/600x400/png?text=Rival+3');

-- SP 5
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Dareu EM908 Victor', 'dareu-em908', 290000, 'https://placehold.co/600x400/png?text=Dareu+EM908', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='dareu-em908'), 'DR-EM908', 290000, '{"color": "Black", "rgb": true}', 'https://placehold.co/600x400/png?text=Dareu+EM908');

-- SP 6
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Logitech G502 Hero', 'logitech-g502', 990000, 'https://placehold.co/600x400/png?text=Logitech+G502', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='logitech-g502'), 'G502-HERO', 990000, '{"buttons": 11, "weight": "Adjustable"}', 'https://placehold.co/600x400/png?text=G502+Hero');

-- SP 7
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Razer Viper Mini', 'viper-mini', 550000, 'https://placehold.co/600x400/png?text=Razer+Viper+Mini', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='viper-mini'), 'RZ-VIPER-MINI', 550000, '{"weight": "61g", "switch": "Optical"}', 'https://placehold.co/600x400/png?text=Viper+Mini');

-- SP 8
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Akko AG325 Dragon Ball', 'akko-ag325', 490000, 'https://placehold.co/600x400/png?text=Akko+DragonBall', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='akko-ag325'), 'AKKO-GOKU', 490000, '{"theme": "Goku", "color": "Orange"}', 'https://placehold.co/600x400/png?text=Akko+Goku');

-- SP 9
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Fuhlen G90', 'fuhlen-g90', 350000, 'https://placehold.co/600x400/png?text=Fuhlen+G90', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='fuhlen-g90'), 'FUHLEN-G90', 350000, '{"switch": "Magnet", "click": "100M"}', 'https://placehold.co/600x400/png?text=Fuhlen+G90');

-- SP 10
INSERT INTO products (name, slug, base_price, thumbnail, category_id) 
VALUES ('Asus TUF Gaming M3', 'asus-m3', 320000, 'https://placehold.co/600x400/png?text=Asus+TUF+M3', 2);
INSERT INTO product_variants (product_id, sku, price, attributes, image_url) VALUES 
((SELECT id FROM products WHERE slug='asus-m3'), 'ASUS-M3', 320000, '{"sensor": "7000dpi", "weight": "84g"}', 'https://placehold.co/600x400/png?text=Asus+M3');

-- 4. Auto Insert Inventory (Tự động tạo kho cho tất cả variant)
INSERT INTO inventory (variant_id, quantity, version)
SELECT id, 100, 0 FROM product_variants;