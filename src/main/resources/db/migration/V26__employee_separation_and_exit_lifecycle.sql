-- ═════════════════════════════════════════════════════════════════════════════
-- V26: Employee Separation, Resignation & Exit Lifecycle Management
-- Supports Employee Self-Service Resignation, Admin-Driven Termination,
-- Notice Period Policy, Departmental Clearances, and Automated User Deactivation.
-- ═════════════════════════════════════════════════════════════════════════════

-- 1. Company Separation Policy
CREATE TABLE IF NOT EXISTS separation_policies (
    id                           BIGSERIAL PRIMARY KEY,
    company_id                   BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    default_notice_days          INT NOT NULL DEFAULT 60,
    probation_notice_days        INT NOT NULL DEFAULT 30,
    allow_resignation_withdrawal BOOLEAN NOT NULL DEFAULT TRUE,
    auto_deactivate_user         BOOLEAN NOT NULL DEFAULT TRUE,
    require_manager_approval     BOOLEAN NOT NULL DEFAULT TRUE,
    require_it_clearance         BOOLEAN NOT NULL DEFAULT TRUE,
    require_finance_clearance    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_separation_policy_company UNIQUE (company_id)
);

-- Seed default separation policies for existing companies
INSERT INTO separation_policies (company_id, default_notice_days, probation_notice_days, allow_resignation_withdrawal, auto_deactivate_user, require_manager_approval, require_it_clearance, require_finance_clearance)
SELECT id, 60, 30, TRUE, TRUE, TRUE, TRUE, TRUE
FROM companies
ON CONFLICT (company_id) DO NOTHING;

-- 2. Separation Cases (Resignations & Terminations)
CREATE TABLE IF NOT EXISTS separation_cases (
    id                           BIGSERIAL PRIMARY KEY,
    company_id                   BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    employee_id                  BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    case_type                    VARCHAR(35) NOT NULL, -- RESIGNATION, TERMINATION, MUTUAL_SEPARATION, END_OF_CONTRACT
    initiator_role               VARCHAR(25) NOT NULL, -- EMPLOYEE, HR_ADMIN, SUPER_ADMIN, MANAGER
    initiator_user_id            BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reason_category              VARCHAR(60) NOT NULL, -- CAREER_GROWTH, PERSONAL_REASONS, BETTER_OFFER, RELOCATION, HIGHER_STUDIES, PERFORMANCE, MISCONDUCT, REDUNDANCY, RETIREMENT, OTHER
    reason_details               TEXT,
    submission_date              DATE NOT NULL DEFAULT CURRENT_DATE,
    requested_last_working_day   DATE NOT NULL,
    approved_last_working_day    DATE,
    notice_period_days           INT NOT NULL DEFAULT 60,
    is_notice_waived             BOOLEAN NOT NULL DEFAULT FALSE,
    notice_shortfall_days        INT NOT NULL DEFAULT 0,
    status                       VARCHAR(35) NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED, NOTICE_PERIOD, CLEARANCE_PENDING, SETTLEMENT_PENDING, EXITED, WITHDRAWN, REJECTED
    handover_notes               TEXT,
    
    -- Manager Multi-tier Review
    manager_approval_status      VARCHAR(25) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, SKIPPED
    manager_remarks              TEXT,
    manager_reviewed_at          TIMESTAMPTZ,
    manager_reviewed_by          BIGINT REFERENCES users(id) ON DELETE SET NULL,
    
    -- HR Operations Review & Authorization
    hr_approval_status           VARCHAR(25) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    hr_remarks                   TEXT,
    hr_reviewed_at               TIMESTAMPTZ,
    hr_reviewed_by               BIGINT REFERENCES users(id) ON DELETE SET NULL,
    
    -- Final Exit & Deactivation Details
    exit_interview_completed     BOOLEAN NOT NULL DEFAULT FALSE,
    exit_interview_notes         TEXT,
    user_deactivated_at          TIMESTAMPTZ,
    finalized_by                 BIGINT REFERENCES users(id) ON DELETE SET NULL,
    finalized_at                 TIMESTAMPTZ,

    created_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_separation_cases_company_status ON separation_cases(company_id, status);
CREATE INDEX IF NOT EXISTS idx_separation_cases_employee ON separation_cases(employee_id);

-- 3. Departmental Exit Clearances
CREATE TABLE IF NOT EXISTS exit_clearances (
    id                           BIGSERIAL PRIMARY KEY,
    separation_case_id           BIGINT NOT NULL REFERENCES separation_cases(id) ON DELETE CASCADE,
    company_id                   BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    department_type              VARCHAR(35) NOT NULL, -- REPORTING_MANAGER, IT_INFRA, ASSETS_ADMIN, FINANCE_ACCOUNTS, HR_OPS
    title                        VARCHAR(100) NOT NULL,
    description                  TEXT,
    status                       VARCHAR(25) NOT NULL DEFAULT 'PENDING', -- PENDING, CLEARED, WAIVED, FLAGGED
    cleared_by_user_id           BIGINT REFERENCES users(id) ON DELETE SET NULL,
    cleared_at                   TIMESTAMPTZ,
    deduction_amount             NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    remarks                      TEXT,
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_exit_clearances_case ON exit_clearances(separation_case_id);
