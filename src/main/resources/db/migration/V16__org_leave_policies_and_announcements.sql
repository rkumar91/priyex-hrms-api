-- ═══════════════════════════════════════════════════════════════════════════
-- V16: Organization Leave Policies & Quotas and Seed Announcements
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS org_leave_policies (
    id                      BIGSERIAL       PRIMARY KEY,
    company_id              BIGINT          NOT NULL DEFAULT 1 REFERENCES companies(id),
    leave_code              VARCHAR(30)     NOT NULL,
    leave_name              VARCHAR(100)    NOT NULL,
    annual_days             INT             NOT NULL DEFAULT 12,
    is_paid                 BOOLEAN         NOT NULL DEFAULT TRUE,
    carry_forward_allowed   BOOLEAN         NOT NULL DEFAULT FALSE,
    max_carry_forward_days  INT             NOT NULL DEFAULT 0,
    description             VARCHAR(500),
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_org_leave_policies UNIQUE (company_id, leave_code)
);

-- Seed Initial Corporate Leave Policies for Company 1
INSERT INTO org_leave_policies (
    company_id, leave_code, leave_name, annual_days, is_paid, carry_forward_allowed, max_carry_forward_days, description
) VALUES
    (1, 'CL', 'Casual Leave (CL)', 12, TRUE, FALSE, 0, 'Annual casual leaves for personal exigencies and short planned breaks.'),
    (1, 'SL', 'Sick Leave (SL)', 10, TRUE, FALSE, 0, 'Medical and sickness leave with health recovery entitlement.'),
    (1, 'PL', 'Privilege Leave (PL / Earned Leave)', 15, TRUE, TRUE, 10, 'Privilege/Earned leaves accrued annually for vacation and long planned absences.'),
    (1, 'MATERNITY', 'Maternity Leave', 180, TRUE, FALSE, 0, 'Statutory maternity leave entitlement for eligible female employees.'),
    (1, 'PATERNITY', 'Paternity Leave', 15, TRUE, FALSE, 0, 'Paternity leave for new fathers upon childbirth.'),
    (1, 'BEREAVEMENT', 'Bereavement Leave', 5, TRUE, FALSE, 0, 'Compassionate leave granted in bereavement of immediate family.')
ON CONFLICT (company_id, leave_code) DO UPDATE
SET annual_days = EXCLUDED.annual_days,
    leave_name = EXCLUDED.leave_name,
    description = EXCLUDED.description;

-- Seed Initial Organization Announcements for Company 1
INSERT INTO announcements (
    company_id, title, body, priority, target_audience, is_published, published_at, created_by
) VALUES
    (1, '2026 Annual Leave Policies Published', 'HR has published the annual leave entitlements including Casual (CL: 12 days), Sick (SL: 10 days), and Privilege (PL: 15 days) leaves under Organization settings.', 'NORMAL', 'ALL', TRUE, NOW(), 1),
    (1, 'Quarterly Town Hall & All-Hands Meeting', 'Join our leadership team this Friday at 4:00 PM IST for our company-wide town hall and quarterly milestones review.', 'HIGH', 'ALL', TRUE, NOW(), 1),
    (1, 'Scheduled HRMS Maintenance Notice', 'Routine platform infrastructure updates are scheduled for Sunday 02:00 AM - 04:00 AM IST. Expect brief intermittent connectivity.', 'URGENT', 'ALL', TRUE, NOW(), 1)
ON CONFLICT DO NOTHING;
