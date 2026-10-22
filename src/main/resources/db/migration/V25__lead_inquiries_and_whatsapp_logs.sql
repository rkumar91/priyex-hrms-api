-- ═══════════════════════════════════════════════════════════════════════════
-- V25: Enterprise Demo Inquiries, Public Leads & WhatsApp Delivery Logs
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Public Demo Requests & Sales Inquiries ──
CREATE TABLE IF NOT EXISTS lead_inquiries (
    id                  BIGSERIAL       PRIMARY KEY,
    inquiry_ref         VARCHAR(50)     NOT NULL UNIQUE,
    full_name           VARCHAR(120)    NOT NULL,
    work_email          VARCHAR(150)    NOT NULL,
    company_name        VARCHAR(150)    NOT NULL,
    phone_number        VARCHAR(30),
    employee_count      VARCHAR(50),
    interest_module     VARCHAR(100),
    inquiry_type        VARCHAR(50)     NOT NULL DEFAULT 'DEMO_REQUEST',
    preferred_date      VARCHAR(30),
    preferred_time_slot VARCHAR(50),
    message             TEXT,
    status              VARCHAR(30)     NOT NULL DEFAULT 'NEW',  -- NEW, QUALIFIED, DEMO_SCHEDULED, CONVERTED, CLOSED
    assigned_consultant VARCHAR(120),
    email_status        VARCHAR(20)     DEFAULT 'PENDING',        -- PENDING, SENT, FAILED
    whatsapp_status     VARCHAR(20)     DEFAULT 'PENDING',        -- PENDING, SENT, MOCK_SENT, SKIPPED, FAILED
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lead_inquiries_ref ON lead_inquiries(inquiry_ref);
CREATE INDEX IF NOT EXISTS idx_lead_inquiries_email ON lead_inquiries(work_email);
CREATE INDEX IF NOT EXISTS idx_lead_inquiries_status ON lead_inquiries(status);
CREATE INDEX IF NOT EXISTS idx_lead_inquiries_created ON lead_inquiries(created_at);

-- ── WhatsApp Delivery Audit Logs ──
CREATE TABLE IF NOT EXISTS whatsapp_delivery_logs (
    id                  BIGSERIAL       PRIMARY KEY,
    inquiry_ref         VARCHAR(50),
    recipient_phone     VARCHAR(30)     NOT NULL,
    template_name       VARCHAR(100)    NOT NULL,
    message_body        TEXT            NOT NULL,
    provider            VARCHAR(30)     NOT NULL DEFAULT 'META_CLOUD', -- META_CLOUD, TWILIO, MOCK
    message_id          VARCHAR(100),
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',    -- SENT, MOCK_SENT, FAILED
    error_message       VARCHAR(500),
    sent_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_whatsapp_delivery_phone ON whatsapp_delivery_logs(recipient_phone);
CREATE INDEX IF NOT EXISTS idx_whatsapp_delivery_ref ON whatsapp_delivery_logs(inquiry_ref);
CREATE INDEX IF NOT EXISTS idx_whatsapp_delivery_created ON whatsapp_delivery_logs(created_at);
