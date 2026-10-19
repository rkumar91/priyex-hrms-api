-- ═══════════════════════════════════════════════════════════════════════════
-- V21: Multi-Client Enterprise Tenants, Policy Engine Settings & Corporate Seeds
-- Supports client-specific rules (TCS, Wipro, Infosys, Priyex)
-- ═══════════════════════════════════════════════════════════════════════════

-- ── 1. Clean up placeholder multi-org companies from V17 if present ──
DELETE FROM companies WHERE code IN ('PRIYEX-DIGITAL', 'PRIYEX-CONSULT');

-- ── Seed Enterprise Client Organizations ──
INSERT INTO companies (id, code, legal_name, brand_name, industry, timezone, currency_code, locale,
                       registration_no, tax_id, email, phone, city, state, country, postal_code, is_active)
VALUES 
    (2, 'TCS', 'Tata Consultancy Services Limited', 'TCS', 'IT Services & Consulting',
     'Asia/Kolkata', 'INR', 'en-IN', 'L22210MH1995PLC084781', 'AAACT2221M',
     'contact@tcs.com', '+91-22-67789999', 'Mumbai', 'Maharashtra', 'India', '400001', TRUE),
    (3, 'WIPRO', 'Wipro Limited', 'Wipro', 'Information Technology & Consulting',
     'Asia/Kolkata', 'INR', 'en-IN', 'L32102KA1945PLC020800', 'AAACW3210K',
     'reachus@wipro.com', '+91-80-28440011', 'Bengaluru', 'Karnataka', 'India', '560035', TRUE),
    (4, 'INFOSYS', 'Infosys Limited', 'Infosys', 'Digital Services & Consulting',
     'Asia/Kolkata', 'INR', 'en-IN', 'L85110KA1981PLC013115', 'AAACI8511L',
     'askus@infosys.com', '+91-80-28520261', 'Bengaluru', 'Karnataka', 'India', '560100', TRUE)
ON CONFLICT (id) DO UPDATE
SET code = EXCLUDED.code,
    legal_name = EXCLUDED.legal_name,
    brand_name = EXCLUDED.brand_name,
    industry = EXCLUDED.industry,
    timezone = EXCLUDED.timezone,
    currency_code = EXCLUDED.currency_code,
    locale = EXCLUDED.locale,
    registration_no = EXCLUDED.registration_no,
    tax_id = EXCLUDED.tax_id,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    city = EXCLUDED.city,
    state = EXCLUDED.state,
    country = EXCLUDED.country,
    postal_code = EXCLUDED.postal_code,
    is_active = EXCLUDED.is_active;

-- Reset sequence to prevent collision
SELECT setval('companies_id_seq', (SELECT GREATEST(MAX(id), 4) FROM companies));

-- ── 2. Seed Default Head-Office Branches for Each Client ──
INSERT INTO branches (company_id, code, name, city, state, country, is_head_office)
VALUES
    (2, 'TCS-HQ', 'TCS House - Fort', 'Mumbai', 'Maharashtra', 'India', TRUE),
    (3, 'WIPRO-HQ', 'Sarjapur Campus', 'Bengaluru', 'Karnataka', 'India', TRUE),
    (4, 'INFY-HQ', 'Electronics City Campus', 'Bengaluru', 'Karnataka', 'India', TRUE)
ON CONFLICT DO NOTHING;

-- ── 3. Seed Core Departments for Each Client ──
INSERT INTO departments (company_id, code, name, description)
VALUES
    (2, 'ENG', 'Engineering & Delivery', 'Application Development & Delivery'),
    (2, 'HR', 'Human Resources', 'People Operations & Talent Acquisition'),
    (2, 'BFSI', 'Banking & Financial Services', 'BFSI Domain Solutions'),
    (3, 'CLOUD', 'Cloud Infrastructure', 'Cloud Engineering & Cybersecurity'),
    (3, 'HR', 'Human Resources', 'People Operations'),
    (4, 'AI-DATA', 'AI & Digital Platforms', 'Generative AI & Data Analytics'),
    (4, 'HR', 'Human Resources', 'Talent Fulfillment')
ON CONFLICT DO NOTHING;

-- ── 4. Seed Configurable Policy Engine Settings Per Client ──
-- Helper macro: company_id, key, value, description
INSERT INTO company_settings (company_id, setting_key, setting_value, description)
VALUES
    -- ── Company 1: Priyex (Modern Hybrid Tech Policy) ──
    (1, 'LEAVE_CALENDAR_CYCLE', 'CALENDAR_YEAR', 'Leave quota cycle: CALENDAR_YEAR (Jan-Dec) or FINANCIAL_YEAR (Apr-Mar)'),
    (1, 'LEAVE_SANDWICH_RULE', 'DISABLED', 'Sandwich rule: if enabled, weekends falling between leave days count as leaves'),
    (1, 'PROBATION_LEAVE_ALLOWED', 'true', 'Whether employees in probation period can apply for leaves'),
    (1, 'ATTENDANCE_GRACE_MINS', '20', 'Grace period in minutes allowed after shift start before marked late'),
    (1, 'HALF_DAY_MIN_HOURS', '4.0', 'Minimum hours worked to qualify for half-day credit'),
    (1, 'FULL_DAY_MIN_HOURS', '8.0', 'Minimum hours worked to qualify for full-day credit'),
    (1, 'GEOFENCE_ENFORCEMENT', 'STRICT', 'Geofencing check strictness: STRICT, FLEXIBLE, or DISABLED'),
    (1, 'OVERTIME_MULTIPLIER_WEEKDAY', '1.5', 'Overtime wage rate multiplier for weekdays'),
    (1, 'OVERTIME_MULTIPLIER_WEEKEND', '2.0', 'Overtime wage rate multiplier for weekends/holidays'),
    (1, 'COFF_VALIDITY_DAYS', '60', 'Calendar days compensatory-off credit remains valid before auto-expiring'),
    (1, 'COFF_MIN_HOURS_FULL_DAY', '8.0', 'Minimum holiday/weekend hours worked to earn 1.0 C-Off day credit'),
    (1, 'APPROVAL_TIER_COUNT', '2', 'Number of approval tiers for requests: 1 (Direct HR), 2 (Manager->HR), 3 (Lead->Manager->HR)'),

    -- ── Company 2: TCS (Strict Enterprise & Statutory Policy) ──
    (2, 'LEAVE_CALENDAR_CYCLE', 'FINANCIAL_YEAR', 'Leave quota cycle: CALENDAR_YEAR (Jan-Dec) or FINANCIAL_YEAR (Apr-Mar)'),
    (2, 'LEAVE_SANDWICH_RULE', 'ENABLED', 'Sandwich rule: if enabled, weekends falling between leave days count as leaves'),
    (2, 'PROBATION_LEAVE_ALLOWED', 'false', 'Whether employees in probation period can apply for leaves'),
    (2, 'ATTENDANCE_GRACE_MINS', '15', 'Grace period in minutes allowed after shift start before marked late'),
    (2, 'HALF_DAY_MIN_HOURS', '4.5', 'Minimum hours worked to qualify for half-day credit'),
    (2, 'FULL_DAY_MIN_HOURS', '8.5', 'Minimum hours worked to qualify for full-day credit'),
    (2, 'GEOFENCE_ENFORCEMENT', 'STRICT', 'Geofencing check strictness: STRICT, FLEXIBLE, or DISABLED'),
    (2, 'OVERTIME_MULTIPLIER_WEEKDAY', '1.5', 'Overtime wage rate multiplier for weekdays'),
    (2, 'OVERTIME_MULTIPLIER_WEEKEND', '2.0', 'Overtime wage rate multiplier for weekends/holidays'),
    (2, 'COFF_VALIDITY_DAYS', '60', 'Calendar days compensatory-off credit remains valid before auto-expiring'),
    (2, 'COFF_MIN_HOURS_FULL_DAY', '8.5', 'Minimum holiday/weekend hours worked to earn 1.0 C-Off day credit'),
    (2, 'APPROVAL_TIER_COUNT', '3', 'Number of approval tiers for requests: 1 (Direct HR), 2 (Manager->HR), 3 (Lead->Manager->HR)'),

    -- ── Company 3: Wipro (Flexible Global Services Policy) ──
    (3, 'LEAVE_CALENDAR_CYCLE', 'CALENDAR_YEAR', 'Leave quota cycle: CALENDAR_YEAR (Jan-Dec) or FINANCIAL_YEAR (Apr-Mar)'),
    (3, 'LEAVE_SANDWICH_RULE', 'DISABLED', 'Sandwich rule: if enabled, weekends falling between leave days count as leaves'),
    (3, 'PROBATION_LEAVE_ALLOWED', 'true', 'Whether employees in probation period can apply for leaves'),
    (3, 'ATTENDANCE_GRACE_MINS', '30', 'Grace period in minutes allowed after shift start before marked late'),
    (3, 'HALF_DAY_MIN_HOURS', '4.0', 'Minimum hours worked to qualify for half-day credit'),
    (3, 'FULL_DAY_MIN_HOURS', '8.0', 'Minimum hours worked to qualify for full-day credit'),
    (3, 'GEOFENCE_ENFORCEMENT', 'FLEXIBLE', 'Geofencing check strictness: STRICT, FLEXIBLE, or DISABLED'),
    (3, 'OVERTIME_MULTIPLIER_WEEKDAY', '1.5', 'Overtime wage rate multiplier for weekdays'),
    (3, 'OVERTIME_MULTIPLIER_WEEKEND', '2.0', 'Overtime wage rate multiplier for weekends/holidays'),
    (3, 'COFF_VALIDITY_DAYS', '90', 'Calendar days compensatory-off credit remains valid before auto-expiring'),
    (3, 'COFF_MIN_HOURS_FULL_DAY', '8.0', 'Minimum holiday/weekend hours worked to earn 1.0 C-Off day credit'),
    (3, 'APPROVAL_TIER_COUNT', '2', 'Number of approval tiers for requests: 1 (Direct HR), 2 (Manager->HR), 3 (Lead->Manager->HR)'),

    -- ── Company 4: Infosys (Digital Workspace & Extended C-Off Policy) ──
    (4, 'LEAVE_CALENDAR_CYCLE', 'CALENDAR_YEAR', 'Leave quota cycle: CALENDAR_YEAR (Jan-Dec) or FINANCIAL_YEAR (Apr-Mar)'),
    (4, 'LEAVE_SANDWICH_RULE', 'DISABLED', 'Sandwich rule: if enabled, weekends falling between leave days count as leaves'),
    (4, 'PROBATION_LEAVE_ALLOWED', 'true', 'Whether employees in probation period can apply for leaves'),
    (4, 'ATTENDANCE_GRACE_MINS', '20', 'Grace period in minutes allowed after shift start before marked late'),
    (4, 'HALF_DAY_MIN_HOURS', '4.0', 'Minimum hours worked to qualify for half-day credit'),
    (4, 'FULL_DAY_MIN_HOURS', '8.0', 'Minimum hours worked to qualify for full-day credit'),
    (4, 'GEOFENCE_ENFORCEMENT', 'FLEXIBLE', 'Geofencing check strictness: STRICT, FLEXIBLE, or DISABLED'),
    (4, 'OVERTIME_MULTIPLIER_WEEKDAY', '1.5', 'Overtime wage rate multiplier for weekdays'),
    (4, 'OVERTIME_MULTIPLIER_WEEKEND', '2.0', 'Overtime wage rate multiplier for weekends/holidays'),
    (4, 'COFF_VALIDITY_DAYS', '120', 'Calendar days compensatory-off credit remains valid before auto-expiring'),
    (4, 'COFF_MIN_HOURS_FULL_DAY', '8.0', 'Minimum holiday/weekend hours worked to earn 1.0 C-Off day credit'),
    (4, 'APPROVAL_TIER_COUNT', '2', 'Number of approval tiers for requests: 1 (Direct HR), 2 (Manager->HR), 3 (Lead->Manager->HR)')
ON CONFLICT (company_id, setting_key) DO UPDATE
SET setting_value = EXCLUDED.setting_value,
    description = EXCLUDED.description,
    updated_at = NOW();

-- ── 5. Seed Client-Specific Org Leave Entitlements ──
INSERT INTO org_leave_policies (company_id, leave_code, leave_name, annual_days, is_paid, carry_forward_allowed, max_carry_forward_days, description)
VALUES
    -- TCS Leave Quota (18 PL, 12 CL, 10 SL)
    (2, 'CL', 'Casual Leave (CL)', 12, TRUE, FALSE, 0, 'Annual casual leaves for personal emergencies'),
    (2, 'SL', 'Sick Leave (SL)', 10, TRUE, FALSE, 0, 'Medical certificate required for > 2 consecutive days'),
    (2, 'PL', 'Privilege Leave (PL)', 18, TRUE, TRUE, 15, 'Earned leave with up to 15 days rollover'),
    (2, 'MATERNITY', 'Maternity Leave', 180, TRUE, FALSE, 0, '26 weeks statutory maternity benefit'),

    -- Wipro Leave Quota (15 PL, 10 CL, 12 SL)
    (3, 'CL', 'Casual Leave (CL)', 10, TRUE, FALSE, 0, 'Short exigencies leave'),
    (3, 'SL', 'Sick Leave (SL)', 12, TRUE, FALSE, 0, 'Medical leave'),
    (3, 'PL', 'Privilege Leave (PL)', 15, TRUE, TRUE, 12, 'Vacation leave with rollover'),
    (3, 'MATERNITY', 'Maternity Leave', 180, TRUE, FALSE, 0, '26 weeks statutory maternity benefit'),

    -- Infosys Leave Quota (15 PL, 12 CL, 10 SL)
    (4, 'CL', 'Casual Leave (CL)', 12, TRUE, FALSE, 0, 'Casual leave quota'),
    (4, 'SL', 'Sick Leave (SL)', 10, TRUE, FALSE, 0, 'Medical sick leave'),
    (4, 'PL', 'Privilege Leave (PL)', 15, TRUE, TRUE, 10, 'Privilege annual vacation leave'),
    (4, 'MATERNITY', 'Maternity Leave', 180, TRUE, FALSE, 0, '26 weeks statutory maternity benefit')
ON CONFLICT (company_id, leave_code) DO NOTHING;
