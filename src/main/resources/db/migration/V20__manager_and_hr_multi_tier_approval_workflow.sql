-- ═══════════════════════════════════════════════════════════════════════════
-- V20: Multi-Tier Approval Workflow (Direct Manager -> HR Final Approval)
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Enhance leave_requests for multi-level tracking
ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS manager_id BIGINT REFERENCES employees(id);
ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS manager_comment TEXT;
ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS manager_approved_at TIMESTAMPTZ;
ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS hr_comment TEXT;
ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS hr_approved_by BIGINT REFERENCES users(id);
ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS hr_approved_at TIMESTAMPTZ;

-- Establish realistic hierarchy for demonstration:
-- Employee 3 (Amit Verma) reports to Employee 1 (Rajesh Kumar)
-- Employee 1 (Rajesh Kumar) reports to Employee 2 (Priya Sharma)
-- Employee 2 (Priya Sharma) reports to Employee 3
UPDATE employees SET reporting_manager_id = 2 WHERE id = 1;
UPDATE employees SET reporting_manager_id = 1 WHERE id = 3;

-- Sync existing leave_requests:
UPDATE leave_requests
SET manager_id = (SELECT reporting_manager_id FROM employees WHERE employees.id = leave_requests.employee_id)
WHERE manager_id IS NULL;

-- Migrate existing status: 'PENDING' -> 'PENDING_MANAGER' (or 'PENDING_HR' if no manager)
UPDATE leave_requests 
SET status = 'PENDING_MANAGER' 
WHERE status = 'PENDING' AND manager_id IS NOT NULL;

UPDATE leave_requests 
SET status = 'PENDING_HR' 
WHERE status = 'PENDING' AND manager_id IS NULL;

-- 2. Create persistent expense_claims table
CREATE TABLE IF NOT EXISTS expense_claims (
    id                  BIGSERIAL PRIMARY KEY,
    claim_number        VARCHAR(50) NOT NULL UNIQUE,
    company_id          BIGINT NOT NULL DEFAULT 1 REFERENCES companies(id),
    employee_id         BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    category            VARCHAR(50) NOT NULL,
    merchant            VARCHAR(150),
    amount              NUMERIC(12,2) NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'INR',
    expense_date        DATE NOT NULL,
    submitted_at        VARCHAR(50),
    description         TEXT,
    project_code        VARCHAR(50),
    gstin               VARCHAR(30),
    receipt_name        VARCHAR(255),
    receipt_url         VARCHAR(500),
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING_MANAGER',
    manager_id          BIGINT REFERENCES employees(id),
    manager_comment     TEXT,
    manager_approved_at TIMESTAMPTZ,
    hr_comment          TEXT,
    hr_approved_by      BIGINT REFERENCES users(id),
    hr_approved_at      TIMESTAMPTZ,
    reimbursed_date     DATE,
    reimbursement_ref   VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_expense_claims_company ON expense_claims(company_id);
CREATE INDEX IF NOT EXISTS idx_expense_claims_employee ON expense_claims(employee_id);
CREATE INDEX IF NOT EXISTS idx_expense_claims_manager ON expense_claims(manager_id);
CREATE INDEX IF NOT EXISTS idx_expense_claims_status ON expense_claims(status);

-- Seed initial demonstration expense claims
INSERT INTO expense_claims (
    id, claim_number, company_id, employee_id, category, merchant,
    amount, currency, expense_date, submitted_at, description,
    project_code, receipt_name, status, manager_id, manager_comment, manager_approved_at,
    hr_comment, hr_approved_at
)
VALUES
    (1, 'EXP-2026-081', 1, 1, 'TRAVEL', 'Air India / Indigo Express',
     14850.00, 'INR', '2026-10-10', '2026-10-11', 'Roundtrip flights for on-site client kickoff meeting in Bengaluru.',
     'PRJ-FINTECH-01', 'Flight_Tickets_Indigo_BLR.pdf', 'APPROVED', 2, 'Verified travel dates and itinerary.', NOW() - INTERVAL '3 days',
     'Approved for payout per travel policy band 4.', NOW() - INTERVAL '2 days'),

    (2, 'EXP-2026-082', 1, 3, 'CLIENT_ENTERTAINMENT', 'The Oberoi Grand Hotel',
     6420.00, 'INR', '2026-10-12', '2026-10-13', 'Executive campus recruitment partner dinner and leadership discussion.',
     'HR-TALENT-2026', 'Oberoi_Invoice_RecruitersDinner.pdf', 'PENDING_MANAGER', 1, NULL, NULL, NULL, NULL),

    (3, 'EXP-2026-083', 1, 1, 'MOBILE_INTERNET', 'Airtel Broadband Fiber',
     1799.00, 'INR', '2026-10-03', '2026-10-04', 'Monthly remote WFH high-speed fiber internet reimbursement allowance.',
     'CORP-WFH-ALLOWANCE', 'Airtel_Bill_Oct2026.pdf', 'REIMBURSED', 2, 'Approved WFH recurring allowance.', NOW() - INTERVAL '10 days',
     'Processed via September payroll cycle.', NOW() - INTERVAL '9 days'),

    (4, 'EXP-2026-084', 1, 3, 'TRAINING', 'AWS Cloud Certification Portal',
     12500.00, 'INR', '2026-10-07', '2026-10-08', 'AWS Certified Solutions Architect Exam registration fees.',
     'DEV-UPSKILL-Q4', 'AWS_Exam_Receipt_12500.pdf', 'PENDING_HR', 1, 'Approved under tech skills enhancement budget.', NOW() - INTERVAL '1 day',
     NULL, NULL)
ON CONFLICT (id) DO NOTHING;

SELECT setval('expense_claims_id_seq', (SELECT COALESCE(MAX(id), 10) FROM expense_claims));
