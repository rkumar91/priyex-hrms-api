-- ═══════════════════════════════════════════════════════════════════════════
-- V14: Expand photo_url to TEXT to support Base64 data URLs and avatar storage
-- ═══════════════════════════════════════════════════════════════════════════

ALTER TABLE employees ALTER COLUMN photo_url TYPE TEXT;
