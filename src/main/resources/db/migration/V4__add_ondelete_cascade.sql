-- Member
ALTER TABLE storage_members
DROP FOREIGN KEY fk_storage_members_user;

ALTER TABLE storage_members
ADD CONSTRAINT fk_storage_members_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;


ALTER TABLE storage_members
DROP FOREIGN KEY fk_storage_members_storage;

ALTER TABLE storage_members
ADD CONSTRAINT fk_storage_members_storage
FOREIGN KEY (storage_id)
REFERENCES storages(id)
ON DELETE CASCADE;


-- Storages
ALTER TABLE storages
DROP FOREIGN KEY fk_storages_owner;

ALTER TABLE storages
ADD CONSTRAINT fk_storages_owner
FOREIGN KEY (owner_id)
REFERENCES users(id)
ON DELETE CASCADE;


-- Products
ALTER TABLE products
DROP FOREIGN KEY fk_products_owner;

ALTER TABLE products
ADD CONSTRAINT fk_products_owner
FOREIGN KEY (owner_id)
REFERENCES users(id)
ON DELETE CASCADE;


-- Storage Item
ALTER TABLE storage_items
DROP FOREIGN KEY fk_storage_items_storage;

ALTER TABLE storage_items
ADD CONSTRAINT fk_storage_items_storage
FOREIGN KEY (storage_id)
REFERENCES storages(id)
ON DELETE CASCADE;


ALTER TABLE storage_items
DROP FOREIGN KEY fk_storage_items_product;

ALTER TABLE storage_items
ADD CONSTRAINT fk_storage_items_product
FOREIGN KEY (product_id)
REFERENCES products(id)
ON DELETE CASCADE;

-- Running Low
ALTER TABLE running_low_settings
DROP FOREIGN KEY fk_running_low_storage;

ALTER TABLE running_low_settings
ADD CONSTRAINT fk_running_low_storage
FOREIGN KEY (storage_id)
REFERENCES storages(id)
ON DELETE CASCADE;


ALTER TABLE running_low_settings
DROP FOREIGN KEY fk_running_low_product;

ALTER TABLE running_low_settings
ADD CONSTRAINT fk_running_low_product
FOREIGN KEY (product_id)
REFERENCES products(id)
ON DELETE CASCADE;


-- Shopping Item
ALTER TABLE shopping_list_items
DROP FOREIGN KEY fk_shopping_storage;

ALTER TABLE shopping_list_items
ADD CONSTRAINT fk_shopping_storage
FOREIGN KEY (storage_id)
REFERENCES storages(id)
ON DELETE CASCADE;


ALTER TABLE shopping_list_items
DROP FOREIGN KEY fk_shopping_product;

ALTER TABLE shopping_list_items
ADD CONSTRAINT fk_shopping_product
FOREIGN KEY (product_id)
REFERENCES products(id)
ON DELETE CASCADE;