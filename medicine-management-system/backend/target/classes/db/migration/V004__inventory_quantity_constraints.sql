-- SYSTEM INVARIANT: Quantity Constraints
-- Add CHECK constraint to ensure inventory quantities cannot go negative

ALTER TABLE inventories 
ADD CONSTRAINT chk_quantity_non_negative 
CHECK (quantity >= 0);

ALTER TABLE inventories
ADD CONSTRAINT chk_threshold_non_negative
CHECK (low_stock_threshold >= 0);
