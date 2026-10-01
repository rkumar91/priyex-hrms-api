-- ═══════════════════════════════════════════════════════════════════════════
-- V17: Investment Declarations, Multi-Org Tax Setup & Statutory Forms
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Ensure Additional Companies Exist for Multi-Organization Scaling
INSERT INTO companies (id, code, legal_name, brand_name, industry, timezone, currency_code, locale, is_active)
VALUES 
    (2, 'PRIYEX-DIGITAL', 'Priyex Digital Solutions Private Limited', 'Priyex Digital', 'Digital Transformation', 'Asia/Kolkata', 'INR', 'en-IN', TRUE),
    (3, 'PRIYEX-CONSULT', 'Priyex Global Consulting Private Limited', 'Priyex Consulting', 'Strategic Advisory', 'Asia/Kolkata', 'INR', 'en-IN', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Reset companies sequence
SELECT setval('companies_id_seq', (SELECT GREATEST(MAX(id), 3) FROM companies));

-- 2. Investment Declarations Table
CREATE TABLE IF NOT EXISTS employee_investment_declarations (
    id                              BIGSERIAL       PRIMARY KEY,
    company_id                      BIGINT          NOT NULL DEFAULT 1 REFERENCES companies(id),
    employee_id                     BIGINT          NOT NULL REFERENCES employees(id),
    financial_year                  VARCHAR(20)     NOT NULL, -- '2026-2027', '2025-2026'
    assessment_year                 VARCHAR(20)     NOT NULL, -- '2027-2028', '2026-2027'
    regime                          VARCHAR(10)     NOT NULL DEFAULT 'NEW', -- 'NEW', 'OLD'
    status                          VARCHAR(50)     NOT NULL DEFAULT 'DRAFT', -- 'DRAFT', 'SUBMITTED', 'VERIFIED', 'REJECTED'

    -- Section 80C Deductions (Max ₹1,50,000)
    sec_80c_epf                     NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80c_ppf                     NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80c_elss                    NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80c_life_insurance          NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80c_housing_principal       NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80c_tuition_fees            NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80c_nsc_fd                  NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80c_total                   NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80c_eligible                NUMERIC(15,2)   NOT NULL DEFAULT 0.00,

    -- Section 80CCD(1B) - NPS (Max ₹50,000)
    sec_80ccd_nps                   NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80ccd_eligible              NUMERIC(15,2)   NOT NULL DEFAULT 0.00,

    -- Section 80D - Medical / Health Insurance
    sec_80d_self_family             NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80d_parents                 NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80d_preventive_checkup      NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80d_total                   NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80d_eligible                NUMERIC(15,2)   NOT NULL DEFAULT 0.00,

    -- Section 24(b) - Home Loan Interest (Max ₹2,00,000 for self-occupied)
    sec_24_home_loan_interest       NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_24_eligible                 NUMERIC(15,2)   NOT NULL DEFAULT 0.00,

    -- Section 10(13A) - House Rent Allowance (HRA)
    annual_rent_paid                NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    landlord_name                   VARCHAR(150),
    landlord_pan                    VARCHAR(20),
    rental_city_type                VARCHAR(20)     NOT NULL DEFAULT 'METRO', -- 'METRO', 'NON_METRO'
    hra_exemption_eligible          NUMERIC(15,2)   NOT NULL DEFAULT 0.00,

    -- Other Deductions
    sec_80e_education_loan          NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80g_donations               NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    sec_80tta_savings_interest      NUMERIC(15,2)   NOT NULL DEFAULT 0.00,

    -- Summary & Audit
    total_declared_deductions       NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    total_eligible_deductions       NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    remarks                         TEXT,
    hr_notes                        TEXT,
    submitted_at                    TIMESTAMPTZ,
    verified_at                     TIMESTAMPTZ,
    verified_by                     BIGINT,
    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_emp_fy UNIQUE (employee_id, financial_year)
);

CREATE INDEX IF NOT EXISTS idx_decl_emp ON employee_investment_declarations(employee_id);
CREATE INDEX IF NOT EXISTS idx_decl_fy ON employee_investment_declarations(financial_year);
CREATE INDEX IF NOT EXISTS idx_decl_comp ON employee_investment_declarations(company_id);

-- 3. Seed Realistic Investment Declarations for Demo Employees (FY 2026-2027)
-- EMP-1001 (Rajesh Kumar - Lead Architect, ₹24 LPA)
INSERT INTO employee_investment_declarations (
    company_id, employee_id, financial_year, assessment_year, regime, status,
    sec_80c_epf, sec_80c_ppf, sec_80c_elss, sec_80c_life_insurance, sec_80c_housing_principal, sec_80c_total, sec_80c_eligible,
    sec_80ccd_nps, sec_80ccd_eligible,
    sec_80d_self_family, sec_80d_parents, sec_80d_preventive_checkup, sec_80d_total, sec_80d_eligible,
    sec_24_home_loan_interest, sec_24_eligible,
    annual_rent_paid, landlord_name, landlord_pan, rental_city_type, hra_exemption_eligible,
    sec_80tta_savings_interest,
    total_declared_deductions, total_eligible_deductions, remarks, hr_notes, submitted_at, verified_at, verified_by
)
SELECT 
    1, e.id, '2026-2027', '2027-2028', 'NEW', 'VERIFIED',
    129600.00, 50000.00, 40000.00, 25000.00, 60000.00, 304600.00, 150000.00,
    50000.00, 50000.00,
    25000.00, 40000.00, 5000.00, 70000.00, 65000.00,
    180000.00, 180000.00,
    360000.00, 'Kailash Sharma', 'ABCPS1234F', 'METRO', 240000.00,
    10000.00,
    974600.00, 695000.00,
    'Annual investment proofs uploaded with LIC receipts, PPF passbook and Home Loan interest certificate.',
    'Verified with bank statement and loan interest certificate. All eligible deductions approved.',
    NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days', 1
FROM employees e WHERE e.employee_code = 'EMP-1001'
ON CONFLICT (employee_id, financial_year) DO NOTHING;

-- EMP-1002 (Priya Sharma - HR Manager, ₹18 LPA)
INSERT INTO employee_investment_declarations (
    company_id, employee_id, financial_year, assessment_year, regime, status,
    sec_80c_epf, sec_80c_ppf, sec_80c_elss, sec_80c_life_insurance, sec_80c_total, sec_80c_eligible,
    sec_80ccd_nps, sec_80ccd_eligible,
    sec_80d_self_family, sec_80d_parents, sec_80d_total, sec_80d_eligible,
    annual_rent_paid, landlord_name, landlord_pan, rental_city_type, hra_exemption_eligible,
    total_declared_deductions, total_eligible_deductions, remarks, hr_notes, submitted_at, verified_at, verified_by
)
SELECT 
    1, e.id, '2026-2027', '2027-2028', 'NEW', 'VERIFIED',
    97200.00, 30000.00, 35000.00, 20000.00, 182200.00, 150000.00,
    40000.00, 40000.00,
    20000.00, 30000.00, 50000.00, 50000.00,
    240000.00, 'Mohit Gupta', 'GPTMH9876K', 'METRO', 180000.00,
    512200.00, 420000.00,
    'Uploaded ELSS tax statements, mediclaim and rent agreement.',
    'All proofs verified and accepted.',
    NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days', 1
FROM employees e WHERE e.employee_code = 'EMP-1002'
ON CONFLICT (employee_id, financial_year) DO NOTHING;

-- EMP-1003 (Amit Verma - Product Manager, ₹16 LPA)
INSERT INTO employee_investment_declarations (
    company_id, employee_id, financial_year, assessment_year, regime, status,
    sec_80c_epf, sec_80c_ppf, sec_80c_total, sec_80c_eligible,
    sec_80d_self_family, sec_80d_total, sec_80d_eligible,
    annual_rent_paid, landlord_name, landlord_pan, rental_city_type, hra_exemption_eligible,
    total_declared_deductions, total_eligible_deductions, remarks, submitted_at
)
SELECT 
    1, e.id, '2026-2027', '2027-2028', 'NEW', 'SUBMITTED',
    86400.00, 60000.00, 146400.00, 146400.00,
    18000.00, 18000.00, 18000.00,
    216000.00, 'Suresh Verma', 'VRMSH7890P', 'METRO', 160000.00,
    380400.00, 324400.00,
    'Submitted initial tax investment declaration for FY 2026-27.',
    NOW() - INTERVAL '5 days'
FROM employees e WHERE e.employee_code = 'EMP-1003'
ON CONFLICT (employee_id, financial_year) DO NOTHING;

-- 4. Seed Additional Historical Payslips for Filtering
-- July 2026 (Month 7, Year 2026)
INSERT INTO payroll_runs (
    id, company_id, payroll_month, payroll_year, payroll_name, total_employees, 
    total_gross, total_deductions, total_net, status, disbursement_date, processed_by, created_at, updated_at
) VALUES 
(
    3, 1, 7, 2026, 'July 2026 Monthly Payroll', 3,
    483333.00, 71500.00, 411833.00, 'DISBURSED', '2026-07-31', 1, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '60 days'
)
ON CONFLICT (company_id, payroll_month, payroll_year) DO NOTHING;

INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    1, 3, e.id, 7, 2026, 'July 2026',
    31, 31, 0, 2400000.00, 200000.00,
    90000.00, 45000.00, 50000.00, 5000.00, 5000.00, 5000.00, 200000.00,
    10800.00, 0.00, 200.00, 26600.00, 37600.00,
    10800.00, 0.00, 4328.00, 162400.00, 'PAID', '2026-07-31', 'NEFT', 'NEFT-HDFC-20260731-1001'
FROM employees e WHERE e.employee_code = 'EMP-1001'
ON CONFLICT (employee_id, payroll_month, payroll_year) DO NOTHING;

-- Also seed payslip for EMP-1002 in July 2026
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    1, 3, e.id, 7, 2026, 'July 2026',
    31, 31, 0, 1800000.00, 150000.00,
    67500.00, 33750.00, 38750.00, 5000.00, 5000.00, 0.00, 150000.00,
    8100.00, 0.00, 200.00, 14200.00, 22500.00,
    8100.00, 0.00, 3246.00, 127500.00, 'PAID', '2026-07-31', 'NEFT', 'NEFT-ICIC-20260731-1002'
FROM employees e WHERE e.employee_code = 'EMP-1002'
ON CONFLICT (employee_id, payroll_month, payroll_year) DO NOTHING;

-- Reset sequence for employee_investment_declarations
SELECT setval('employee_investment_declarations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM employee_investment_declarations));
