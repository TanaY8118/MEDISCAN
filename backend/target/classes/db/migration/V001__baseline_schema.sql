-- V001__baseline_schema.sql
-- Authoritative relational schema for Mediscan backend
-- This migration defines the full MySQL schema used by JPA.

-- USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- GROUPS TABLE (named user_groups to avoid reserved keyword conflicts)
CREATE TABLE IF NOT EXISTS user_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_groups_owner
        FOREIGN KEY (owner_id) REFERENCES users(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX idx_user_groups_owner_id ON user_groups(owner_id);

-- INVENTORIES TABLE
CREATE TABLE IF NOT EXISTS inventories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id VARCHAR(64) NOT NULL,
    quantity INT NOT NULL,
    low_stock_threshold INT NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'unit(s)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT chk_threshold_non_negative CHECK (low_stock_threshold >= 0)
);

CREATE INDEX idx_inventories_medicine_id ON inventories(medicine_id);

-- INVENTORY LOGS TABLE
CREATE TABLE IF NOT EXISTS inventory_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    change_amount INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    note VARCHAR(255),
    performed_by VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_inventory_logs_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventories(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE INDEX idx_inventory_logs_inventory_id_timestamp
    ON inventory_logs(inventory_id, timestamp);

