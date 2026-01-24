-- Add unit column to inventories
ALTER TABLE inventories ADD COLUMN unit VARCHAR(20) DEFAULT 'unit(s)';

-- No changes needed to inventory_logs table for the relationship, 
-- as it already has inventory_id which will be mapped via @ManyToOne.
