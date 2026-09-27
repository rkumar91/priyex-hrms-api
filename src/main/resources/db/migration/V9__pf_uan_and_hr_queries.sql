-- ═══════════════════════════════════════════════════════════════════════════
-- V9: PF & UAN Statutory Details, HR Query Ticketing System
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Add PF and UAN statutory columns to employees table
ALTER TABLE employees ADD COLUMN IF NOT EXISTS pf_number VARCHAR(50);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS uan_number VARCHAR(50);

-- Populate demo PF and UAN numbers for demo employees
UPDATE employees SET pf_number = 'UP/NOI/0034182/000/0001001', uan_number = '101489201928' WHERE id = 1 AND (pf_number IS NULL OR uan_number IS NULL);
UPDATE employees SET pf_number = 'UP/NOI/0034182/000/0001002', uan_number = '101489201929' WHERE id = 2 AND (pf_number IS NULL OR uan_number IS NULL);
UPDATE employees SET pf_number = 'DL/CPM/0029103/000/0001003', uan_number = '101489201930' WHERE id = 3 AND (pf_number IS NULL OR uan_number IS NULL);

-- 2. Create HR Queries / Tickets table
CREATE TABLE IF NOT EXISTS hr_queries (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL DEFAULT 1 REFERENCES companies(id),
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    assigned_hr_id  BIGINT          REFERENCES users(id),
    category        VARCHAR(50)     NOT NULL, -- PAYROLL, PF_UAN, LEAVE, TAX, GENERAL
    subject         VARCHAR(200)    NOT NULL,
    message         TEXT            NOT NULL,
    priority        VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    status          VARCHAR(30)     NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    hr_response     TEXT,
    responded_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_hr_queries_company ON hr_queries(company_id);
CREATE INDEX IF NOT EXISTS idx_hr_queries_employee ON hr_queries(employee_id);
CREATE INDEX IF NOT EXISTS idx_hr_queries_hr ON hr_queries(assigned_hr_id);
CREATE INDEX IF NOT EXISTS idx_hr_queries_status ON hr_queries(status);

-- 3. Seed sample queries
INSERT INTO hr_queries (id, company_id, employee_id, assigned_hr_id, category, subject, message, priority, status, hr_response, responded_at)
VALUES
    (1, 1, 1, (SELECT id FROM users WHERE email = 'hr@priyex.com' LIMIT 1), 'PF_UAN', 'Clarification on previous company PF transfer to Priyex UAN', 
     'Hello HR Team, I have initiated an online EPF transfer on the EPFO member portal for my previous employer service. Could you please approve the transfer request from the employer DSC portal?',
     'HIGH', 'OPEN', NULL, NULL),
    (2, 1, 1, (SELECT id FROM users WHERE email = 'hr@priyex.com' LIMIT 1), 'PAYROLL', 'Monthly Payslip Tax & PF Deduction Query', 
     'Hi Priya, noticed standard deduction line item under the new tax regime. Could you please clarify if investment proofs need re-upload?',
     'MEDIUM', 'RESOLVED', 'Hi Rajesh, verified your tax regime choice. The standard deduction of Rs 75,000 is automatically factored in under the new tax regime. No additional proof submission required.', NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('hr_queries_id_seq', (SELECT COALESCE(MAX(id), 1) FROM hr_queries));
