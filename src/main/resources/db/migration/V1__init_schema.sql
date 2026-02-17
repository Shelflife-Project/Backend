CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    is_admin BIT(1) NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255),
    mimetype VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    barcode VARCHAR(255) UNIQUE,
    category VARCHAR(255) NOT NULL,
    expiration_days_delta INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_products_owner
        FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS storages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_storages_owner
        FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS storage_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    is_accepted BIT(1) NOT NULL,
    storage_id BIGINT,
    user_id BIGINT,
    CONSTRAINT fk_storage_members_storage
        FOREIGN KEY (storage_id) REFERENCES storages(id),
    CONSTRAINT fk_storage_members_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS storage_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    expires_at DATE NOT NULL,
    product_id BIGINT,
    storage_id BIGINT,
    CONSTRAINT fk_storage_items_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_storage_items_storage
        FOREIGN KEY (storage_id) REFERENCES storages(id)
);

CREATE TABLE IF NOT EXISTS running_low_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    running_low INT NOT NULL,
    product_id BIGINT,
    storage_id BIGINT,
    UNIQUE KEY uk_storage_product (storage_id, product_id),
    CONSTRAINT fk_running_low_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_running_low_storage
        FOREIGN KEY (storage_id) REFERENCES storages(id)
);

CREATE TABLE IF NOT EXISTS invalidjwts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    token VARCHAR(255)
);
