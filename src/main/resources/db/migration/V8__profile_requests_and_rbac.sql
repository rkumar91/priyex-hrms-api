-- ═══════════════════════════════════════════════════════════════════════════
-- V8: Profile Requests, Address/Bank Direct Access & Enhanced RBAC
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Add direct address and bank fields on employees table for fast self-service access
ALTER TABLE employees ADD COLUMN IF NOT EXISTS address_line1 VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS state VARCHAR(100);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_account_number VARCHAR(100);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_ifsc VARCHAR(50);

-- 2. Ensure initial demo employees exist with realistic details
INSERT INTO employees (
    id, company_id, employee_code, user_id, first_name, last_name,
    work_email, personal_phone, department_id, designation_id,
    branch_id, employment_type, joining_date, status,
    address_line1, city, state, postal_code, bank_account_number, bank_ifsc
)
VALUES
    (1, 1, 'EMP-1001', 2, 'Rajesh', 'Kumar',
     'rajesh.kumar@priyex.com', '+91 9876543210', 1, 8,
     1, 'FULL_TIME', '2023-01-15', 'ACTIVE',
     'Flat 402, Green Valley Apartments', 'Greater Noida', 'Uttar Pradesh', '201306',
     'HDFC - 5010098234123', 'HDFC0001234'),
    (2, 1, 'EMP-1002', 3, 'Priya', 'Sharma',
     'priya.sharma@priyex.com', '+91 9876543211', 2, 12,
     1, 'FULL_TIME', '2023-03-01', 'ACTIVE',
     'House 12, Sector 62', 'Noida', 'Uttar Pradesh', '201301',
     'ICICI - 002105001928', 'ICIC0000021'),
    (3, 1, 'EMP-1003', NULL, 'Amit', 'Verma',
     'amit.verma@priyex.com', '+91 9876543212', 1, 9,
     1, 'FULL_TIME', '2023-06-10', 'ON_LEAVE',
     'Pocket B, Mayur Vihar', 'New Delhi', 'Delhi', '110091',
     'SBI - 30981240912', 'SBIN0001290')
ON CONFLICT (id) DO UPDATE SET
    address_line1 = COALESCE(employees.address_line1, EXCLUDED.address_line1),
    city = COALESCE(employees.city, EXCLUDED.city),
    state = COALESCE(employees.state, EXCLUDED.state),
    postal_code = COALESCE(employees.postal_code, EXCLUDED.postal_code),
    bank_account_number = COALESCE(employees.bank_account_number, EXCLUDED.bank_account_number),
    bank_ifsc = COALESCE(employees.bank_ifsc, EXCLUDED.bank_ifsc);

SELECT setval('employees_id_seq', (SELECT COALESCE(MAX(id), 1) FROM employees));

-- 3. Link user accounts to corresponding employee records
UPDATE users SET employee_id = 1 WHERE email = 'employee@priyex.com';
UPDATE users SET employee_id = 2 WHERE email = 'hr@priyex.com';
UPDATE users SET employee_id = 3 WHERE email = 'admin@priyex.com';

-- 4. Create Employee Profile Requests table for HR Approval Workflow
CREATE TABLE IF NOT EXISTS employee_profile_requests (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL DEFAULT 1 REFERENCES companies(id),
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    request_type    VARCHAR(50)     NOT NULL, -- BANK_ACCOUNT, CONTACT_NAME, EMAIL, OTHER
    field_name      VARCHAR(100)    NOT NULL,
    current_value   TEXT,
    requested_value TEXT            NOT NULL,
    reason          TEXT,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    reviewer_id     BIGINT          REFERENCES users(id),
    reviewer_notes  TEXT,
    reviewed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_emp_requests_company ON employee_profile_requests(company_id);
CREATE INDEX IF NOT EXISTS idx_emp_requests_employee ON employee_profile_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_emp_requests_status ON employee_profile_requests(status);

-- 5. Seed initial pending change requests for HR approval
INSERT INTO employee_profile_requests (id, company_id, employee_id, request_type, field_name, current_value, requested_value, reason, status)
VALUES 
    (1, 1, 1, 'BANK_ACCOUNT', 'Bank Account Number', 'HDFC - 5010098234123', 'Axis Bank - 921010048192834 (IFSC: UTIB0000123)', 'Salary account switch to corporate Axis Bank account.', 'PENDING'),
    (2, 1, 1, 'CONTACT_NAME', 'Legal Full Name', 'Rajesh Kumar', 'Rajesh K. Sharma', 'Name update as per revised Government PAN Card.', 'PENDING')
ON CONFLICT (id) DO NOTHING;

SELECT setval('employee_profile_requests_id_seq', (SELECT COALESCE(MAX(id), 1) FROM employee_profile_requests));
