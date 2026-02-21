-- Add amount_to_buy column to storage_items
ALTER TABLE storage_items
ADD COLUMN amount_to_buy integer NOT NULL DEFAULT 0;
