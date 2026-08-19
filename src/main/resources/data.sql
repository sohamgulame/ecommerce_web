-- ============================================================
-- Complete Dev Seed & Fix Script for Categories, Products & Images
-- ============================================================

-- 1. Ensure Categories Exist
INSERT INTO categories (name, description) VALUES
('Electronics & Computing', 'Laptops, monitors, PC components, and high-tech peripherals'),
('Audio & Wearables', 'Noise-cancelling headphones, wireless earbuds, and smart accessories'),
('Fashion & Apparel', 'Modern minimalist apparel, outerwear, and comfortable footwear'),
('Home & Kitchen', 'Smart appliances, barista essentials, and contemporary home decor'),
('Books & Stationery', 'Software engineering bestsellers, tech guides, and premium journals')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 2. Link Categories to Existing Products by Name
UPDATE products p JOIN categories c ON c.name = 'Electronics & Computing'
SET p.category_id = c.id
WHERE p.name LIKE '%MacBook%' OR p.name LIKE '%Monitor%' OR p.name LIKE '%Keyboard%' OR p.name LIKE '%Mouse%';

UPDATE products p JOIN categories c ON c.name = 'Audio & Wearables'
SET p.category_id = c.id
WHERE p.name LIKE '%Sony%' OR p.name LIKE '%AirPods%' OR p.name LIKE '%Smartwatch%' OR p.name LIKE '%Headphone%';

UPDATE products p JOIN categories c ON c.name = 'Fashion & Apparel'
SET p.category_id = c.id
WHERE p.name LIKE '%Denim%' OR p.name LIKE '%Hoodie%' OR p.name LIKE '%Running Shoes%' OR p.name LIKE '%Sneakers%';

UPDATE products p JOIN categories c ON c.name = 'Home & Kitchen'
SET p.category_id = c.id
WHERE p.name LIKE '%Espresso%' OR p.name LIKE '%Bottle%' OR p.name LIKE '%Desk Lamp%' OR p.name LIKE '%Lamp%';

UPDATE products p JOIN categories c ON c.name = 'Books & Stationery'
SET p.category_id = c.id
WHERE p.name LIKE '%Clean Architecture%' OR p.name LIKE '%Journal%' OR p.name LIKE '%Book%';

-- 3. Clear and Populate Images (product_images table)
DELETE FROM product_images;

-- MacBook Pro 16" M3 Max
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%MacBook%' LIMIT 1;

-- Ultra-Wide Curved Gaming Monitor 34"
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Monitor%' LIMIT 1;

-- Custom Mechanical Keyboard RGB / Keyboard
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Keyboard%' LIMIT 2;

-- Ergonomic Precision Wireless Mouse / Pro Ergonomic Wireless Mouse
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Mouse%' LIMIT 2;

-- Sony WH-1000XM5 Wireless Headphones
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Sony%' LIMIT 1;

-- AirPods Pro (2nd Generation)
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%AirPods%' LIMIT 1;

-- GPS Fitness Smartwatch Series 9
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Smartwatch%' LIMIT 1;

-- Classic Washed Denim Jacket
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Denim%' LIMIT 1;

-- Premium Organic Cotton Hoodie
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Hoodie%' LIMIT 1;

-- All-Terrain Cushioned Running Shoes
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Running Shoes%' OR name LIKE '%Sneakers%' LIMIT 1;

-- Precision Automatic Espresso Machine
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Espresso%' LIMIT 1;

-- Insulated Stainless Steel Bottle (1L)
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Bottle%' LIMIT 1;

-- Dimmable Minimalist Desk Lamp
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Lamp%' LIMIT 1;

-- Clean Architecture by Robert C. Martin
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Clean Architecture%' LIMIT 1;

-- Hardcover Dot-Grid Leather Journal
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?auto=format&fit=crop&w=800&q=80'
FROM products WHERE name LIKE '%Journal%' LIMIT 1;
