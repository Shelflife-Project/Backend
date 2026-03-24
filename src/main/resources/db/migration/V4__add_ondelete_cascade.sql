ALTER TABLE storage_member
DROP CONSTRAINT IF EXISTS fk_storage_member_user,
ADD CONSTRAINT fk_storage_member_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE storage_member
DROP CONSTRAINT IF EXISTS fk_storage_member_storage,
ADD CONSTRAINT fk_storage_member_storage
FOREIGN KEY (storage_id)
REFERENCES storages(id)
ON DELETE CASCADE;


ALTER TABLE storages
DROP CONSTRAINT IF EXISTS fk_storages_user,
ADD CONSTRAINT fk_storages_user
FOREIGN KEY (owner_id)
REFERENCES users(id)
ON DELETE CASCADE;


ALTER TABLE products
DROP CONSTRAINT IF EXISTS fk_products_user,
ADD CONSTRAINT fk_products_user
FOREIGN KEY (owner_id)
REFERENCES users(id)
ON DELETE CASCADE;


ALTER TABLE storage_item
DROP CONSTRAINT IF EXISTS fk_storage_item_storage,
ADD CONSTRAINT fk_storage_item_storage
FOREIGN KEY (storage_id)
REFERENCES storages(id)
ON DELETE CASCADE;

ALTER TABLE storage_item
DROP CONSTRAINT IF EXISTS fk_storage_item_product,
ADD CONSTRAINT fk_storage_item_product
FOREIGN KEY (product_id)
REFERENCES products(id)
ON DELETE CASCADE;


ALTER TABLE running_low_setting
DROP CONSTRAINT IF EXISTS fk_running_low_setting_storage,
ADD CONSTRAINT fk_running_low_setting_storage
FOREIGN KEY (storage_id)
REFERENCES storages(id)
ON DELETE CASCADE;

ALTER TABLE running_low_setting
DROP CONSTRAINT IF EXISTS fk_running_low_setting_product,
ADD CONSTRAINT fk_running_low_setting_product
FOREIGN KEY (product_id)
REFERENCES products(id)
ON DELETE CASCADE;


ALTER TABLE shopping_list_item
DROP CONSTRAINT IF EXISTS fk_shopping_list_item_storage,
ADD CONSTRAINT fk_shopping_list_item_storage
FOREIGN KEY (storage_id)
REFERENCES storages(id)
ON DELETE CASCADE;

ALTER TABLE shopping_list_item
DROP CONSTRAINT IF EXISTS fk_shopping_list_item_product,
ADD CONSTRAINT fk_shopping_list_item_product
FOREIGN KEY (product_id)
REFERENCES products(id)
ON DELETE CASCADE;