-- ═══════════════════════════════════════════════════════════════════════════
-- V15: Expand bank_account_number to VARCHAR(255) for comprehensive bank info
-- ═══════════════════════════════════════════════════════════════════════════

ALTER TABLE employees ALTER COLUMN bank_account_number TYPE VARCHAR(255);
