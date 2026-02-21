-- Create shopping_list_items table
CREATE TABLE shopping_list_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  storage_id BIGINT,
  product_id BIGINT,
  amount_to_buy INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uniq_storage_product (storage_id, product_id),
  CONSTRAINT fk_shopping_storage FOREIGN KEY (storage_id) REFERENCES storages(id),
  CONSTRAINT fk_shopping_product FOREIGN KEY (product_id) REFERENCES products(id)
);
