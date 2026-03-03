-- V002__add_user_ownership.sql
-- Add explicit user ownership to relational entities.
-- NOTE: This migration assumes either an empty inventories/inventory_logs table
-- or that data will be backfilled with valid user_id values before applying NOT NULL.

-- Add user ownership to inventories
ALTER TABLE inventories
    ADD COLUMN user_id BIGINT NOT NULL AFTER id;

ALTER TABLE inventories
    ADD CONSTRAINT fk_inventories_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;

CREATE INDEX idx_inventories_user_id
    ON inventories(user_id);

-- Add user ownership to inventory_logs
ALTER TABLE inventory_logs
    ADD COLUMN user_id BIGINT NOT NULL AFTER inventory_id;

ALTER TABLE inventory_logs
    ADD CONSTRAINT fk_inventory_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;

CREATE INDEX idx_inventory_logs_user_id_timestamp
    ON inventory_logs(user_id, timestamp);

