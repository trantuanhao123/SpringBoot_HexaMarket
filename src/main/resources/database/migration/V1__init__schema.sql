-- =============================================
-- 1. AUTHENTICATION & USERS
-- =============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(15),
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    city VARCHAR(100),
    district VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================
-- 2. PRODUCT CATALOG
-- =============================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE,
    description TEXT,
    parent_id BIGINT,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
);

CREATE TABLE products ( 
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    thumbnail VARCHAR(255),
    base_price DECIMAL(15, 2) NOT NULL,
    category_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_variants ( 
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku VARCHAR(50) UNIQUE NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    attributes JSONB, 
    image_url VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- =============================================
-- 3. INVENTORY & ORDERS
-- =============================================
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    variant_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL CHECK (quantity >= 0),
    reserved_quantity INT DEFAULT 0 CHECK (reserved_quantity >= 0),
    version BIGINT DEFAULT 0, 
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_inventory_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    total_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    shipping_address TEXT NOT NULL,
    shipping_phone VARCHAR(15),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    quantity INT NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);

-- =============================================
-- 4. PAYMENT
-- =============================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100),
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- =============================================
-- 5. ADVANCED INDEXING (OPTIMIZED)
-- =============================================

-- Tăng tốc tìm kiếm không phân biệt hoa thường cho Product Name
CREATE INDEX idx_products_name_lower ON products (LOWER(name));

-- Tăng tốc lọc sản phẩm theo Category kết hợp trạng thái (Composite Index)
CREATE INDEX idx_products_filter_logic ON products (category_id, is_active, is_deleted);

-- Tăng tốc sắp xếp theo giá và ngày tạo
CREATE INDEX idx_products_price_sort ON products (base_price ASC);
CREATE INDEX idx_products_created_at_desc ON products (created_at DESC);

-- Tối ưu Soft Delete cho Variants và Inventory
CREATE INDEX idx_variants_is_deleted ON product_variants (is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX idx_inventory_is_deleted ON inventory (is_deleted) WHERE is_deleted = FALSE;

-- JSONB GIN Index để search thuộc tính (DPI, Color, Size...)
CREATE INDEX idx_variants_attributes_gin ON product_variants USING GIN (attributes);

-- Partial Index cho địa chỉ mặc định (truy vấn cực nhanh cho 1 địa chỉ/1 user)
CREATE INDEX idx_user_address_default ON user_addresses (user_id) WHERE is_default = TRUE;

-- Tối ưu quản lý đơn hàng theo khách hàng và ngày tạo
CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);
CREATE INDEX idx_orders_status ON orders (status);

-- Tối ưu cho User search & Auth
CREATE INDEX idx_users_email ON users (email);