-- ═══════════════════════════════════════════════════════════════════════════
-- V18: Scheduled banner announcements (display window = published_at .. expires_at)
-- ═══════════════════════════════════════════════════════════════════════════

-- Backfill any legacy rows without a start time so window filtering is consistent
UPDATE announcements SET published_at = created_at WHERE published_at IS NULL;

-- Speeds up the "currently live" lookup used on every poll
CREATE INDEX IF NOT EXISTS idx_announcements_window
    ON announcements (company_id, published_at, expires_at);
