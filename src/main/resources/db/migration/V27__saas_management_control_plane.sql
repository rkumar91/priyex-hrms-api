-- ═════════════════════════════════════════════════════════════════════════════
-- V27: Priyex SaaS Management Console Control Plane
-- Implements commercial account relationships, versioned plans, subscriptions,
-- billing invoice snapshots, customer pre/post-sale requests, and audit logs.
-- ═════════════════════════════════════════════════════════════════════════════

-- 1. Commercial Customer Accounts (Distinct from HRMS tenant entity)
CREATE TABLE IF NOT EXISTS saas_commercial_accounts (
    id                   BIGSERIAL PRIMARY KEY,
    account_ref          VARCHAR(50) NOT NULL UNIQUE,
    account_name         VARCHAR(150) NOT NULL,
    legal_name           VARCHAR(180) NOT NULL,
    billing_country      VARCHAR(100) NOT NULL DEFAULT 'India',
    billing_currency     VARCHAR(10)  NOT NULL DEFAULT 'INR',
    business_domain      VARCHAR(120),
    primary_contact_name VARCHAR(120) NOT NULL,
    primary_contact_email VARCHAR(150) NOT NULL,
    primary_contact_phone VARCHAR(30),
    assigned_owner       VARCHAR(120) DEFAULT 'tech.priyex@gmail.com',
    lifecycle_status     VARCHAR(30)  NOT NULL DEFAULT 'PROSPECT', -- PROSPECT, EVALUATION, CONTRACTING, ACTIVE, AT_RISK, OFFBOARDED, CLOSED
    linked_company_id    BIGINT REFERENCES companies(id) ON DELETE SET NULL,
    source_lead_ref      VARCHAR(50),
    annual_contract_value NUMERIC(12, 2) DEFAULT 0.00,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saas_acc_status ON saas_commercial_accounts(lifecycle_status);
CREATE INDEX IF NOT EXISTS idx_saas_acc_company ON saas_commercial_accounts(linked_company_id);

-- 2. Versioned SaaS Plan Catalog
CREATE TABLE IF NOT EXISTS saas_plans (
    id                   BIGSERIAL PRIMARY KEY,
    plan_code            VARCHAR(50) NOT NULL, -- STARTER, GROWTH, ENTERPRISE
    plan_name            VARCHAR(100) NOT NULL,
    version              VARCHAR(20) NOT NULL DEFAULT 'v1.0',
    billing_interval     VARCHAR(20) NOT NULL DEFAULT 'ANNUAL', -- MONTHLY, ANNUAL
    currency             VARCHAR(10) NOT NULL DEFAULT 'INR',
    price_per_seat       NUMERIC(10, 2) NOT NULL DEFAULT 150.00,
    min_seats            INT NOT NULL DEFAULT 10,
    features_summary     TEXT,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_saas_plan_version UNIQUE (plan_code, version, billing_interval)
);

-- 3. Commercial Subscriptions
CREATE TABLE IF NOT EXISTS saas_subscriptions (
    id                   BIGSERIAL PRIMARY KEY,
    subscription_ref     VARCHAR(50) NOT NULL UNIQUE,
    account_id           BIGINT NOT NULL REFERENCES saas_commercial_accounts(id) ON DELETE CASCADE,
    plan_id              BIGINT NOT NULL REFERENCES saas_plans(id),
    status               VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- PENDING, TRIAL, ACTIVE, PAST_DUE, GRACE_PERIOD, SUSPENDED, CANCELLED
    seats_licensed       INT NOT NULL DEFAULT 25,
    period_start         DATE NOT NULL,
    period_end           DATE NOT NULL,
    next_billing_date    DATE NOT NULL,
    auto_renew           BOOLEAN NOT NULL DEFAULT TRUE,
    payment_method       VARCHAR(50) DEFAULT 'INVOICE_TRANSFER', -- CREDIT_CARD, UPI, WIRE_TRANSFER, INVOICE_TRANSFER
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saas_sub_status ON saas_subscriptions(status);

-- 4. Invoices & Billing Operations
CREATE TABLE IF NOT EXISTS saas_invoices (
    id                   BIGSERIAL PRIMARY KEY,
    invoice_number       VARCHAR(50) NOT NULL UNIQUE,
    account_id           BIGINT NOT NULL REFERENCES saas_commercial_accounts(id) ON DELETE CASCADE,
    subscription_id      BIGINT REFERENCES saas_subscriptions(id) ON DELETE SET NULL,
    billing_period_start DATE NOT NULL,
    billing_period_end   DATE NOT NULL,
    subtotal_amount      NUMERIC(12, 2) NOT NULL,
    tax_amount           NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_amount         NUMERIC(12, 2) NOT NULL,
    currency             VARCHAR(10) NOT NULL DEFAULT 'INR',
    due_date             DATE NOT NULL,
    status               VARCHAR(30) NOT NULL DEFAULT 'ISSUED', -- DRAFT, ISSUED, PAID, PAST_DUE, VOID
    paid_at              TIMESTAMPTZ,
    payment_reference    VARCHAR(100),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saas_inv_status ON saas_invoices(status);

-- 5. Customer Pre/Post-Sale Requests & Support Handoff
CREATE TABLE IF NOT EXISTS saas_customer_requests (
    id                   BIGSERIAL PRIMARY KEY,
    request_ref          VARCHAR(50) NOT NULL UNIQUE,
    account_id           BIGINT REFERENCES saas_commercial_accounts(id) ON DELETE SET NULL,
    lead_id              BIGINT REFERENCES lead_inquiries(id) ON DELETE SET NULL,
    contact_name         VARCHAR(120) NOT NULL,
    contact_email        VARCHAR(150) NOT NULL,
    request_type         VARCHAR(50) NOT NULL, -- SALES_QUERY, DEMO_TECHNICAL, FEATURE_REQUEST, INTEGRATION_CUSTOM, BILLING_QUERY, SECURITY_QUESTIONNAIRE
    title                VARCHAR(200) NOT NULL,
    description          TEXT NOT NULL,
    priority             VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    status               VARCHAR(30) NOT NULL DEFAULT 'NEW',    -- NEW, TRIAGED, IN_PROGRESS, ANSWERED, DECLINED, CLOSED
    assigned_to          VARCHAR(120),
    target_date          DATE,
    resolution_notes     TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saas_req_status ON saas_customer_requests(status);
CREATE INDEX IF NOT EXISTS idx_saas_req_type ON saas_customer_requests(request_type);

-- 6. Console Immutable Audit Events
CREATE TABLE IF NOT EXISTS saas_audit_events (
    id                   BIGSERIAL PRIMARY KEY,
    actor_email          VARCHAR(150) NOT NULL,
    action_type          VARCHAR(100) NOT NULL,
    target_entity        VARCHAR(50)  NOT NULL,
    target_id            VARCHAR(100) NOT NULL,
    summary              VARCHAR(300) NOT NULL,
    details_json         TEXT,
    ip_address           VARCHAR(50),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saas_audit_created ON saas_audit_events(created_at DESC);

-- ── SEED INITIAL PLANS ──
INSERT INTO saas_plans (plan_code, plan_name, version, billing_interval, currency, price_per_seat, min_seats, features_summary, is_active)
VALUES
('STARTER', 'Priyex Starter Suite', 'v1.0', 'ANNUAL', 'INR', 99.00, 10, 'Core HR, Attendance Punch, Leave Management, Biometric Webhooks, Standard Payslips', TRUE),
('GROWTH', 'Priyex Enterprise Growth', 'v1.0', 'ANNUAL', 'INR', 149.00, 25, 'Starter Features + Multi-tier Separation Approvals, Investment Declarations, Live Support Desk, Geo-perimeter punch', TRUE),
('ENTERPRISE', 'Priyex Global Sovereign', 'v1.0', 'ANNUAL', 'INR', 249.00, 50, 'Growth Features + Custom Domain Routing, Multi-currency CTC Payroll, WhatsApp Automated Alerts, Dedicated Account SLA', TRUE)
ON CONFLICT (plan_code, version, billing_interval) DO NOTHING;

-- ── SEED INITIAL COMMERCIAL ACCOUNTS LINKED TO EXISTING TENANTS ──
INSERT INTO saas_commercial_accounts (account_ref, account_name, legal_name, billing_country, billing_currency, business_domain, primary_contact_name, primary_contact_email, primary_contact_phone, assigned_owner, lifecycle_status, linked_company_id, annual_contract_value)
SELECT 'PRX-ACC-PRIYEX', 'Priyex HQ', c.legal_name, 'India', 'INR', 'priyex.com', 'Rajesh Kumar', 'tech.priyex@gmail.com', '+919901694683', 'tech.priyex@gmail.com', 'ACTIVE', c.id, 120000.00
FROM companies c WHERE c.code = 'PRIYEX'
ON CONFLICT (account_ref) DO NOTHING;

INSERT INTO saas_commercial_accounts (account_ref, account_name, legal_name, billing_country, billing_currency, business_domain, primary_contact_name, primary_contact_email, primary_contact_phone, assigned_owner, lifecycle_status, linked_company_id, annual_contract_value)
SELECT 'PRX-ACC-TCS', 'Tata Consultancy Services', c.legal_name, 'India', 'INR', 'tcs.com', 'Natarajan Chandrasekaran', 'admin@tcs.com', '+912267789999', 'tech.priyex@gmail.com', 'ACTIVE', c.id, 480000.00
FROM companies c WHERE c.code = 'TCS'
ON CONFLICT (account_ref) DO NOTHING;

INSERT INTO saas_commercial_accounts (account_ref, account_name, legal_name, billing_country, billing_currency, business_domain, primary_contact_name, primary_contact_email, primary_contact_phone, assigned_owner, lifecycle_status, linked_company_id, annual_contract_value)
SELECT 'PRX-ACC-WIPRO', 'Wipro Enterprise', c.legal_name, 'India', 'INR', 'wipro.com', 'Azim Premji', 'admin@wipro.com', '+918028440011', 'tech.priyex@gmail.com', 'ACTIVE', c.id, 350000.00
FROM companies c WHERE c.code = 'WIPRO'
ON CONFLICT (account_ref) DO NOTHING;

INSERT INTO saas_commercial_accounts (account_ref, account_name, legal_name, billing_country, billing_currency, business_domain, primary_contact_name, primary_contact_email, primary_contact_phone, assigned_owner, lifecycle_status, linked_company_id, annual_contract_value)
SELECT 'PRX-ACC-INFOSYS', 'Infosys Systems', c.legal_name, 'India', 'INR', 'infosys.com', 'Salil Parekh', 'admin@infosys.com', '+918028520261', 'tech.priyex@gmail.com', 'ACTIVE', c.id, 420000.00
FROM companies c WHERE c.code = 'INFOSYS'
ON CONFLICT (account_ref) DO NOTHING;

-- ── SEED INITIAL SUBSCRIPTIONS ──
INSERT INTO saas_subscriptions (subscription_ref, account_id, plan_id, status, seats_licensed, period_start, period_end, next_billing_date, auto_renew)
SELECT 'PRX-SUB-TCS-01', acc.id, p.id, 'ACTIVE', 150, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE + INTERVAL '305 days', CURRENT_DATE + INTERVAL '305 days', TRUE
FROM saas_commercial_accounts acc, saas_plans p
WHERE acc.account_ref = 'PRX-ACC-TCS' AND p.plan_code = 'ENTERPRISE'
ON CONFLICT (subscription_ref) DO NOTHING;

INSERT INTO saas_subscriptions (subscription_ref, account_id, plan_id, status, seats_licensed, period_start, period_end, next_billing_date, auto_renew)
SELECT 'PRX-SUB-WIPRO-01', acc.id, p.id, 'ACTIVE', 100, CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE + INTERVAL '320 days', CURRENT_DATE + INTERVAL '320 days', TRUE
FROM saas_commercial_accounts acc, saas_plans p
WHERE acc.account_ref = 'PRX-ACC-WIPRO' AND p.plan_code = 'GROWTH'
ON CONFLICT (subscription_ref) DO NOTHING;

INSERT INTO saas_subscriptions (subscription_ref, account_id, plan_id, status, seats_licensed, period_start, period_end, next_billing_date, auto_renew)
SELECT 'PRX-SUB-INFY-01', acc.id, p.id, 'ACTIVE', 120, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '335 days', CURRENT_DATE + INTERVAL '335 days', TRUE
FROM saas_commercial_accounts acc, saas_plans p
WHERE acc.account_ref = 'PRX-ACC-INFOSYS' AND p.plan_code = 'ENTERPRISE'
ON CONFLICT (subscription_ref) DO NOTHING;

-- ── SEED INITIAL INVOICES ──
INSERT INTO saas_invoices (invoice_number, account_id, subscription_id, billing_period_start, billing_period_end, subtotal_amount, tax_amount, total_amount, currency, due_date, status, paid_at, payment_reference)
SELECT 'PRX-INV-2026-001', sub.account_id, sub.id, sub.period_start, sub.period_end, 448200.00, 80676.00, 528876.00, 'INR', sub.period_start + INTERVAL '15 days', 'PAID', sub.period_start + INTERVAL '3 days', 'HDFC-NEFT-TCS-2026'
FROM saas_subscriptions sub WHERE sub.subscription_ref = 'PRX-SUB-TCS-01'
ON CONFLICT (invoice_number) DO NOTHING;

INSERT INTO saas_invoices (invoice_number, account_id, subscription_id, billing_period_start, billing_period_end, subtotal_amount, tax_amount, total_amount, currency, due_date, status, paid_at, payment_reference)
SELECT 'PRX-INV-2026-002', sub.account_id, sub.id, sub.period_start, sub.period_end, 178800.00, 32184.00, 210984.00, 'INR', sub.period_start + INTERVAL '15 days', 'PAID', sub.period_start + INTERVAL '5 days', 'ICICI-RTGS-WIPRO-2026'
FROM saas_subscriptions sub WHERE sub.subscription_ref = 'PRX-SUB-WIPRO-01'
ON CONFLICT (invoice_number) DO NOTHING;

-- ── SEED SAMPLE CUSTOMER PRE/POST-SALE REQUESTS ──
INSERT INTO saas_customer_requests (request_ref, account_id, contact_name, contact_email, request_type, title, description, priority, status, assigned_to, target_date)
SELECT 'PRX-REQ-2026-01', acc.id, 'K. Ananth Krishnan', 'cto@tcs.com', 'INTEGRATION_CUSTOM', 'Active Directory SSO SAML 2.0 Bridge Integration', 'TCS requires federated enterprise SSO integration with custom group claim mapping for HR managers.', 'HIGH', 'IN_PROGRESS', 'tech.priyex@gmail.com', CURRENT_DATE + INTERVAL '14 days'
FROM saas_commercial_accounts acc WHERE acc.account_ref = 'PRX-ACC-TCS'
ON CONFLICT (request_ref) DO NOTHING;

INSERT INTO saas_customer_requests (request_ref, account_id, contact_name, contact_email, request_type, title, description, priority, status, assigned_to, target_date)
SELECT 'PRX-REQ-2026-02', acc.id, 'Priya Nair', 'priya.nair@wipro.com', 'FEATURE_REQUEST', 'Multi-currency Payroll Settlement in AED & GBP', 'Wipro global teams require salary settlement support in UAE Dirhams and British Pounds with statutory tax breakdown.', 'MEDIUM', 'TRIAGED', 'tech.priyex@gmail.com', CURRENT_DATE + INTERVAL '30 days'
FROM saas_commercial_accounts acc WHERE acc.account_ref = 'PRX-ACC-WIPRO'
ON CONFLICT (request_ref) DO NOTHING;

-- ── SEED INITIAL AUDIT EVENT ──
INSERT INTO saas_audit_events (actor_email, action_type, target_entity, target_id, summary, ip_address)
VALUES ('tech.priyex@gmail.com', 'INITIALIZE_CONTROL_PLANE', 'SYSTEM', 'SAAS_V1', 'Priyex SaaS Management Console Control Plane initialized successfully.', '127.0.0.1');
