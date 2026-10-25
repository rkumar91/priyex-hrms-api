-- ═══════════════════════════════════════════════════════════════════════════
-- V29: Sales Lead Conversation History (Activity Timeline) & Follow-up Tracking
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Follow-up scheduling & contact tracking on each lead
ALTER TABLE lead_inquiries
    ADD COLUMN IF NOT EXISTS next_follow_up_at  TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_contacted_at  TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS drop_reason        VARCHAR(150);

CREATE INDEX IF NOT EXISTS idx_lead_inquiries_followup ON lead_inquiries(next_follow_up_at);

-- 2. Conversation history: every note, call, stage change and drop is recorded
CREATE TABLE IF NOT EXISTS lead_activities (
    id                  BIGSERIAL       PRIMARY KEY,
    lead_id             BIGINT          NOT NULL REFERENCES lead_inquiries(id) ON DELETE CASCADE,
    activity_type       VARCHAR(30)     NOT NULL DEFAULT 'NOTE',   -- NOTE, CALL, EMAIL, WHATSAPP, MEETING, STAGE_CHANGE, DROPPED
    from_status         VARCHAR(30),
    to_status           VARCHAR(30),
    note                TEXT,
    next_follow_up_at   TIMESTAMPTZ,
    created_by          VARCHAR(150)    NOT NULL DEFAULT 'system',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lead_activities_lead ON lead_activities(lead_id, created_at DESC);
