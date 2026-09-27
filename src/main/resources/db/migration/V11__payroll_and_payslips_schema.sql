-- ═══════════════════════════════════════════════════════════════════════════
-- V11: Payroll Management, Runs & Employee CTC / Payslips Schema
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Create payroll_runs table
CREATE TABLE IF NOT EXISTS payroll_runs (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL DEFAULT 1,
    payroll_month INT NOT NULL,
    payroll_year INT NOT NULL,
    payroll_name VARCHAR(150) NOT NULL,
    total_employees INT NOT NULL DEFAULT 0,
    total_gross NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_deductions NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_net NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'PROCESSED', -- DRAFT, PROCESSED, DISBURSED
    disbursement_date DATE,
    processed_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payroll_period UNIQUE (company_id, payroll_month, payroll_year)
);

-- 2. Create employee_payslips table
CREATE TABLE IF NOT EXISTS employee_payslips (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL DEFAULT 1,
    payroll_run_id BIGINT REFERENCES payroll_runs(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    payroll_month INT NOT NULL,
    payroll_year INT NOT NULL,
    pay_period VARCHAR(50) NOT NULL,
    working_days INT DEFAULT 30,
    paid_days INT DEFAULT 30,
    lop_days INT DEFAULT 0,
    
    -- CTC & Salary Structure
    annual_ctc NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    monthly_gross NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    
    -- Earnings Breakdown
    basic_salary NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    hra NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    special_allowance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    medical_allowance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    conveyance_allowance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    performance_bonus NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_earnings NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    
    -- Statutory & Tax Deductions
    epf_employee NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    esic_employee NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    professional_tax NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    tds_tax NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_deductions NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    
    -- Employer Contributions (Part of CTC)
    epf_employer NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    esic_employer NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    gratuity NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    
    -- Net Take-Home Salary
    net_salary NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'PAID', -- PAID, PROCESSED, HELD
    payment_date DATE,
    payment_mode VARCHAR(50) DEFAULT 'NEFT',
    transaction_reference VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_employee_payslip_period UNIQUE (employee_id, payroll_month, payroll_year)
);

CREATE INDEX IF NOT EXISTS idx_employee_payslips_emp ON employee_payslips(employee_id);
CREATE INDEX IF NOT EXISTS idx_employee_payslips_run ON employee_payslips(payroll_run_id);
CREATE INDEX IF NOT EXISTS idx_payroll_runs_comp ON payroll_runs(company_id, payroll_year, payroll_month);

-- 3. Seed Realistic Historical Payroll Runs
INSERT INTO payroll_runs (
    id, company_id, payroll_month, payroll_year, payroll_name, total_employees, 
    total_gross, total_deductions, total_net, status, disbursement_date, processed_by, created_at, updated_at
) VALUES 
(
    1, 1, 9, 2026, 'September 2026 Monthly Payroll', 3,
    483333.00, 71500.00, 411833.00, 'DISBURSED', '2026-09-30', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    2, 1, 8, 2026, 'August 2026 Monthly Payroll', 3,
    483333.00, 71500.00, 411833.00, 'DISBURSED', '2026-08-31', 1, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days'
)
ON CONFLICT (company_id, payroll_month, payroll_year) DO UPDATE SET
    total_gross = EXCLUDED.total_gross,
    total_deductions = EXCLUDED.total_deductions,
    total_net = EXCLUDED.total_net,
    status = EXCLUDED.status;

-- 4. Seed Detailed Payslips for Demo Employees (September 2026)
-- EMP-1001 (Rajesh Kumar - Lead Architect): Annual CTC ₹24,00,000 (Monthly Gross ₹2,00,000)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    1, 1, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 2400000.00, 200000.00,
    90000.00, 45000.00, 50000.00, 5000.00, 5000.00, 5000.00, 200000.00,
    10800.00, 0.00, 200.00, 26600.00, 37600.00,
    10800.00, 0.00, 4328.00, 162400.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-HDFC-20260930-1001'
FROM employees e WHERE e.employee_code = 'EMP-1001'
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- EMP-1002 (Priya Sharma - HR Manager): Annual CTC ₹18,00,000 (Monthly Gross ₹1,50,000)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    1, 1, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1800000.00, 150000.00,
    67500.00, 33750.00, 38750.00, 5000.00, 5000.00, 0.00, 150000.00,
    8100.00, 0.00, 200.00, 14200.00, 22500.00,
    8100.00, 0.00, 3246.00, 127500.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-ICIC-20260930-1002'
FROM employees e WHERE e.employee_code = 'EMP-1002'
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- EMP-1003 (Amit Verma - Product Manager): Annual CTC ₹16,00,000 (Monthly Gross ₹1,33,333)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    1, 1, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1600000.00, 133333.00,
    60000.00, 30000.00, 33333.00, 5000.00, 5000.00, 0.00, 133333.00,
    7200.00, 0.00, 200.00, 10000.00, 17400.00,
    7200.00, 0.00, 2885.00, 115933.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-SBIN-20260930-1003'
FROM employees e WHERE e.employee_code = 'EMP-1003'
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- Also seed August 2026 payslips
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    1, 2, e.id, 8, 2026, 'August 2026',
    31, 31, 0, 2400000.00, 200000.00,
    90000.00, 45000.00, 50000.00, 5000.00, 5000.00, 5000.00, 200000.00,
    10800.00, 0.00, 200.00, 26600.00, 37600.00,
    10800.00, 0.00, 4328.00, 162400.00, 'PAID', '2026-08-31', 'NEFT', 'NEFT-HDFC-20260831-1001'
FROM employees e WHERE e.employee_code = 'EMP-1001'
ON CONFLICT (employee_id, payroll_month, payroll_year) DO NOTHING;

-- Reset sequence
SELECT setval('payroll_runs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM payroll_runs));
SELECT setval('employee_payslips_id_seq', (SELECT COALESCE(MAX(id), 1) FROM employee_payslips));
