-- ═══════════════════════════════════════════════════════════════════════════
-- V28: Setup Fees, Maintenance Contracts (AMC), Drop Tracking & SaaS Staff Roles
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Setup Fees and Maintenance Contracts (AMC / SLA)
CREATE TABLE IF NOT EXISTS saas_contracts (
    id                   BIGSERIAL PRIMARY KEY,
    contract_ref         VARCHAR(50) NOT NULL UNIQUE,
    account_id           BIGINT NOT NULL REFERENCES saas_commercial_accounts(id) ON DELETE CASCADE,
    contract_type        VARCHAR(50) NOT NULL, -- ONE_TIME_SETUP, ANNUAL_MAINTENANCE, PREMIUM_SLA, CUSTOM_DEVELOPMENT
    title                VARCHAR(200) NOT NULL,
    amount               NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    currency             VARCHAR(10) NOT NULL DEFAULT 'INR',
    billing_status       VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, INVOICED, PAID, WAIVED
    start_date           DATE NOT NULL,
    end_date             DATE,
    terms_summary        TEXT,
    invoice_id           BIGINT REFERENCES saas_invoices(id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saas_contracts_account ON saas_contracts(account_id);
CREATE INDEX IF NOT EXISTS idx_saas_contracts_type ON saas_contracts(contract_type);

-- 2. Add drop reason tracking columns to commercial accounts
ALTER TABLE saas_commercial_accounts
    ADD COLUMN IF NOT EXISTS drop_reason VARCHAR(150),
    ADD COLUMN IF NOT EXISTS drop_notes TEXT,
    ADD COLUMN IF NOT EXISTS drop_date DATE;

-- 3. Seed specialized SaaS operational roles into the platform
INSERT INTO roles (company_id, name, display_name, description, is_system, is_active)
VALUES
    (NULL, 'SAAS_ADMIN',   'SaaS Platform Admin',    'Full administrative control over tenant organizations and platform settings', TRUE, TRUE),
    (NULL, 'SAAS_SALES',   'SaaS Sales Specialist',  'Management of inbound inquiries, product demos, proposals and lead pipeline', TRUE, TRUE),
    (NULL, 'SAAS_FINANCE', 'SaaS Revenue & Billing', 'Subscription catalog, invoices, settlements, setup fees, and AMC contracts', TRUE, TRUE),
    (NULL, 'SAAS_SUPPORT', 'SaaS Technical Support', 'Customer tickets, feature requests, custom integrations, and SLA compliance', TRUE, TRUE)
ON CONFLICT (company_id, name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    is_active = TRUE;

-- 4. Seed initial setup and maintenance contracts for existing accounts
INSERT INTO saas_contracts (contract_ref, account_id, contract_type, title, amount, currency, billing_status, start_date, end_date, terms_summary)
SELECT
    'PRX-SETUP-001',
    a.id,
    'ONE_TIME_SETUP',
    'Enterprise Provisioning & Custom Data Migration',
    75000.00,
    'INR',
    'PAID',
    '2026-01-15',
    '2026-02-15',
    'Includes single-sign-on setup, LDAP synchronization, and legacy biometric data migration'
FROM saas_commercial_accounts a WHERE a.account_ref = 'PRX-TCS-001'
ON CONFLICT (contract_ref) DO NOTHING;

INSERT INTO saas_contracts (contract_ref, account_id, contract_type, title, amount, currency, billing_status, start_date, end_date, terms_summary)
SELECT
    'PRX-AMC-001',
    a.id,
    'ANNUAL_MAINTENANCE',
    '24/7 Priority SLA & Dedicated Tech Lead Support',
    120000.00,
    'INR',
    'PAID',
    '2026-02-01',
    '2027-01-31',
    'Guaranteed 15-minute response SLA for critical incidents and quarterly audit reports'
FROM saas_commercial_accounts a WHERE a.account_ref = 'PRX-TCS-001'
ON CONFLICT (contract_ref) DO NOTHING;

INSERT INTO saas_contracts (contract_ref, account_id, contract_type, title, amount, currency, billing_status, start_date, end_date, terms_summary)
SELECT
    'PRX-SETUP-002',
    a.id,
    'ONE_TIME_SETUP',
    'Standard Cloud Deployment & Admin Training',
    35000.00,
    'INR',
    'PAID',
    '2026-03-01',
    '2026-03-15',
    'Includes 3 sessions of admin training and corporate geofence polygon configuration'
FROM saas_commercial_accounts a WHERE a.account_ref = 'PRX-WIPRO-001'
ON CONFLICT (contract_ref) DO NOTHING;
