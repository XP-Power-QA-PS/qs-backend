-- Migration to drop priority column and index from customer_complaints table
DROP INDEX IF EXISTS idx_customer_complaints_priority;
ALTER TABLE customer_complaints DROP COLUMN IF EXISTS priority;
