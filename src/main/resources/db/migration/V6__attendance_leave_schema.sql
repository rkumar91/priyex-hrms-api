-- ═══════════════════════════════════════════════════════════════════════════
-- V6: Attendance & Leave Requests Schema
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS leave_requests (
    id                  BIGSERIAL       PRIMARY KEY,
    company_id          BIGINT          NOT NULL DEFAULT 1,
    employee_id         BIGINT,
    leave_type          VARCHAR(50)     NOT NULL, -- Annual Leave, Casual Leave, Sick Leave, Maternity Leave
    start_date          DATE            NOT NULL,
    end_date            DATE            NOT NULL,
    total_days          INT             NOT NULL DEFAULT 1,
    reason              TEXT,
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, CANCELLED
    approver_comment    TEXT,
    approved_by         BIGINT,
    approved_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Seed Initial Leave Requests
INSERT INTO leave_requests (id, company_id, employee_id, leave_type, start_date, end_date, total_days, reason, status)
VALUES
    (1, 1, 1, 'Annual Leave', '2026-10-01', '2026-10-03', 3, 'Family vacation', 'PENDING'),
    (2, 1, 1, 'Casual Leave', '2026-09-28', '2026-09-28', 1, 'Personal work', 'APPROVED'),
    (3, 1, 1, 'Sick Leave',   '2026-09-25', '2026-09-26', 2, 'Medical recovery', 'APPROVED')
ON CONFLICT (id) DO NOTHING;

SELECT setval('leave_requests_id_seq', (SELECT MAX(id) FROM leave_requests));
