-- SYSTEM INVARIANT: Database Permission Enforcement
-- Create restricted app user with append-only access to inventory_logs

-- Create app user (if not exists)
CREATE USER IF NOT EXISTS 'mediscan_app'@'%' IDENTIFIED BY 'mediscan_app_password';

-- Grant general permissions for most tables
GRANT SELECT, INSERT, UPDATE, DELETE ON mediscan.* TO 'mediscan_app'@'%';

-- CRITICAL RESTRICTION: inventory_logs is APPEND-ONLY
REVOKE UPDATE, DELETE ON mediscan.inventory_logs FROM 'mediscan_app'@'%';
GRANT SELECT, INSERT ON mediscan.inventory_logs TO 'mediscan_app'@'%';

-- Apply changes
FLUSH PRIVILEGES;

-- Verification query
SHOW GRANTS FOR 'mediscan_app'@'%';
