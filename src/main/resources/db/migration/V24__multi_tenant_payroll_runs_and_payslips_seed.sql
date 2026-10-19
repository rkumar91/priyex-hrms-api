-- ═══════════════════════════════════════════════════════════════════════════
-- V24: Seed Multi-Tenant Payroll Runs and Employee Payslips for Client Tenants
-- Generates realistic historical runs & individual payslips for TCS, Wipro, and Infosys
-- ═══════════════════════════════════════════════════════════════════════════

-- Ensure sequence IDs are synchronized to avoid primary key collision
SELECT setval('payroll_runs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM payroll_runs));
SELECT setval('employee_payslips_id_seq', (SELECT COALESCE(MAX(id), 1) FROM employee_payslips));

-- ── 1. TCS (Company ID 2) Payroll Runs ──
INSERT INTO payroll_runs (
    company_id, payroll_month, payroll_year, payroll_name, total_employees,
    total_gross, total_deductions, total_net, status, disbursement_date, processed_by, created_at, updated_at
) VALUES 
(
    2, 9, 2026, 'September 2026 Monthly Payroll - TCS', 4,
    583333.00, 86600.00, 496733.00, 'DISBURSED', '2026-09-30', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    2, 8, 2026, 'August 2026 Monthly Payroll - TCS', 4,
    583333.00, 86600.00, 496733.00, 'DISBURSED', '2026-08-31', 7, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days'
)
ON CONFLICT (company_id, payroll_month, payroll_year) DO UPDATE SET
    total_gross = EXCLUDED.total_gross,
    total_deductions = EXCLUDED.total_deductions,
    total_net = EXCLUDED.total_net,
    status = EXCLUDED.status;

-- ── 2. TCS Employee Payslips (September 2026) ──
-- TCS-001: Rajesh Sharma (Admin) - CTC ₹22,00,000 (Monthly Gross ₹1,83,333.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    2, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 2200000.00, 183333.00,
    82500.00, 41250.00, 44583.00, 5000.00, 5000.00, 5000.00, 183333.00,
    9900.00, 0.00, 200.00, 22500.00, 32600.00,
    9900.00, 0.00, 3968.00, 150733.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-TCS-20260930-001'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'TCS-001' AND r.company_id = 2 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- TCS-002: Ananya Roy (HR) - CTC ₹16,00,000 (Monthly Gross ₹1,33,333.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    2, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1600000.00, 133333.00,
    60000.00, 30000.00, 33333.00, 5000.00, 5000.00, 0.00, 133333.00,
    7200.00, 0.00, 200.00, 12000.00, 19400.00,
    7200.00, 0.00, 2886.00, 113933.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-TCS-20260930-002'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'TCS-002' AND r.company_id = 2 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- TCS-003: Vikram Malhotra (Manager) - CTC ₹18,00,000 (Monthly Gross ₹1,50,000.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    2, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1800000.00, 150000.00,
    67500.00, 33750.00, 38750.00, 5000.00, 5000.00, 0.00, 150000.00,
    8100.00, 0.00, 200.00, 14200.00, 22500.00,
    8100.00, 0.00, 3246.00, 127500.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-TCS-20260930-003'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'TCS-003' AND r.company_id = 2 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- TCS-004: Pooja Verma (Employee) - CTC ₹14,00,000 (Monthly Gross ₹1,16,667.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    2, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1400000.00, 116667.00,
    52500.00, 26250.00, 27917.00, 5000.00, 5000.00, 0.00, 116667.00,
    6300.00, 0.00, 200.00, 5600.00, 12100.00,
    6300.00, 0.00, 2525.00, 104567.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-TCS-20260930-004'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'TCS-004' AND r.company_id = 2 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;


-- ── 3. WIPRO (Company ID 3) Payroll Runs ──
INSERT INTO payroll_runs (
    company_id, payroll_month, payroll_year, payroll_name, total_employees,
    total_gross, total_deductions, total_net, status, disbursement_date, processed_by, created_at, updated_at
) VALUES 
(
    3, 9, 2026, 'September 2026 Monthly Payroll - Wipro', 4,
    566667.00, 83100.00, 483567.00, 'DISBURSED', '2026-09-30', 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    3, 8, 2026, 'August 2026 Monthly Payroll - Wipro', 4,
    566667.00, 83100.00, 483567.00, 'DISBURSED', '2026-08-31', 11, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days'
)
ON CONFLICT (company_id, payroll_month, payroll_year) DO UPDATE SET
    total_gross = EXCLUDED.total_gross,
    total_deductions = EXCLUDED.total_deductions,
    total_net = EXCLUDED.total_net,
    status = EXCLUDED.status;

-- WIPRO Employee Payslips (September 2026)
-- WIP-001: Suresh Nair (Admin) - CTC ₹20,00,000 (Monthly Gross ₹1,66,667.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    3, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 2000000.00, 166667.00,
    75000.00, 37500.00, 39167.00, 5000.00, 5000.00, 5000.00, 166667.00,
    9000.00, 0.00, 200.00, 18500.00, 27700.00,
    9000.00, 0.00, 3608.00, 138967.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-WIP-20260930-001'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'WIP-001' AND r.company_id = 3 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- WIP-002: Kavita Menon (HR) - CTC ₹16,00,000 (Monthly Gross ₹1,33,333.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    3, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1600000.00, 133333.00,
    60000.00, 30000.00, 33333.00, 5000.00, 5000.00, 0.00, 133333.00,
    7200.00, 0.00, 200.00, 12000.00, 19400.00,
    7200.00, 0.00, 2886.00, 113933.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-WIP-20260930-002'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'WIP-002' AND r.company_id = 3 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- WIP-003: Arjun Reddy (Manager) - CTC ₹18,00,000 (Monthly Gross ₹1,50,000.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    3, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1800000.00, 150000.00,
    67500.00, 33750.00, 38750.00, 5000.00, 5000.00, 0.00, 150000.00,
    8100.00, 0.00, 200.00, 14200.00, 22500.00,
    8100.00, 0.00, 3246.00, 127500.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-WIP-20260930-003'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'WIP-003' AND r.company_id = 3 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- WIP-004: Sneha Patel (Employee) - CTC ₹14,00,000 (Monthly Gross ₹1,16,667.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    3, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1400000.00, 116667.00,
    52500.00, 26250.00, 27917.00, 5000.00, 5000.00, 0.00, 116667.00,
    6300.00, 0.00, 200.00, 5600.00, 12100.00,
    6300.00, 0.00, 2525.00, 104567.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-WIP-20260930-004'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'WIP-004' AND r.company_id = 3 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;


-- ── 4. INFOSYS (Company ID 4) Payroll Runs ──
INSERT INTO payroll_runs (
    company_id, payroll_month, payroll_year, payroll_name, total_employees,
    total_gross, total_deductions, total_net, status, disbursement_date, processed_by, created_at, updated_at
) VALUES 
(
    4, 9, 2026, 'September 2026 Monthly Payroll - Infosys', 4,
    600000.00, 91100.00, 508900.00, 'DISBURSED', '2026-09-30', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    4, 8, 2026, 'August 2026 Monthly Payroll - Infosys', 4,
    600000.00, 91100.00, 508900.00, 'DISBURSED', '2026-08-31', 15, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days'
)
ON CONFLICT (company_id, payroll_month, payroll_year) DO UPDATE SET
    total_gross = EXCLUDED.total_gross,
    total_deductions = EXCLUDED.total_deductions,
    total_net = EXCLUDED.total_net,
    status = EXCLUDED.status;

-- INFOSYS Employee Payslips (September 2026)
-- INF-001: Deepak Gupta (Admin) - CTC ₹24,00,000 (Monthly Gross ₹2,00,000.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    4, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 2400000.00, 200000.00,
    90000.00, 45000.00, 50000.00, 5000.00, 5000.00, 5000.00, 200000.00,
    10800.00, 0.00, 200.00, 26600.00, 37600.00,
    10800.00, 0.00, 4328.00, 162400.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-INF-20260930-001'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'INF-001' AND r.company_id = 4 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- INF-002: Priya Iyer (HR) - CTC ₹16,00,000 (Monthly Gross ₹1,33,333.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    4, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1600000.00, 133333.00,
    60000.00, 30000.00, 33333.00, 5000.00, 5000.00, 0.00, 133333.00,
    7200.00, 0.00, 200.00, 12000.00, 19400.00,
    7200.00, 0.00, 2886.00, 113933.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-INF-20260930-002'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'INF-002' AND r.company_id = 4 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- INF-003: Rohan Kulkarni (Manager) - CTC ₹18,00,000 (Monthly Gross ₹1,50,000.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    4, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1800000.00, 150000.00,
    67500.00, 33750.00, 38750.00, 5000.00, 5000.00, 0.00, 150000.00,
    8100.00, 0.00, 200.00, 14200.00, 22500.00,
    8100.00, 0.00, 3246.00, 127500.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-INF-20260930-003'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'INF-003' AND r.company_id = 4 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- INF-004: Aditi Rao (Employee) - CTC ₹15,00,000 (Monthly Gross ₹1,25,000.00)
INSERT INTO employee_payslips (
    company_id, payroll_run_id, employee_id, payroll_month, payroll_year, pay_period,
    working_days, paid_days, lop_days, annual_ctc, monthly_gross,
    basic_salary, hra, special_allowance, medical_allowance, conveyance_allowance, performance_bonus, total_earnings,
    epf_employee, esic_employee, professional_tax, tds_tax, total_deductions,
    epf_employer, esic_employer, gratuity, net_salary, status, payment_date, payment_mode, transaction_reference
)
SELECT 
    4, r.id, e.id, 9, 2026, 'September 2026',
    30, 30, 0, 1500000.00, 125000.00,
    56250.00, 28125.00, 30625.00, 5000.00, 5000.00, 0.00, 125000.00,
    6750.00, 0.00, 200.00, 8500.00, 15450.00,
    6750.00, 0.00, 2705.00, 109550.00, 'PAID', '2026-09-30', 'NEFT', 'NEFT-INF-20260930-004'
FROM employees e, payroll_runs r 
WHERE e.employee_code = 'INF-004' AND r.company_id = 4 AND r.payroll_month = 9 AND r.payroll_year = 2026
ON CONFLICT (employee_id, payroll_month, payroll_year) DO UPDATE SET
    net_salary = EXCLUDED.net_salary,
    total_earnings = EXCLUDED.total_earnings;

-- Ensure sequence counters are updated to latest max
SELECT setval('payroll_runs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM payroll_runs));
SELECT setval('employee_payslips_id_seq', (SELECT COALESCE(MAX(id), 1) FROM employee_payslips));
