-- ═══════════════════════════════════════════════════════════════════════════
-- V5: Development Seed Data
-- WARNING: This is synthetic demo data only. Do NOT use in production.
-- Admin password is BCrypt hash of "Admin@123" — must be changed on first login.
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Demo Company ──
INSERT INTO companies (code, legal_name, brand_name, industry, timezone, currency_code, locale,
                       registration_no, phone, email, address_line1, city, state, country, postal_code)
VALUES ('PRIYEX', 'Priyex Technologies Private Limited', 'Priyex', 'Information Technology',
        'Asia/Kolkata', 'INR', 'en-IN', 'U72900UP2024PTC000001',
        '+91-9901694683', 'info@priyex.com',
        'Nirala Estate', 'Greater Noida', 'Uttar Pradesh', 'India', '201306');

-- ── Demo Branch ──
INSERT INTO branches (company_id, code, name, address_line1, city, state, country, postal_code, is_head_office)
VALUES (1, 'HQ', 'Head Office', 'Nirala Estate', 'Greater Noida', 'Uttar Pradesh', 'India', '201306', TRUE);

-- ── Demo Departments ──
INSERT INTO departments (company_id, code, name, description) VALUES
    (1, 'ENG',  'Engineering',      'Software Development & Engineering'),
    (1, 'HR',   'Human Resources',  'People Operations & HR Management'),
    (1, 'FIN',  'Finance',          'Finance & Accounting'),
    (1, 'MKT',  'Marketing',        'Marketing & Communications'),
    (1, 'OPS',  'Operations',       'Business Operations'),
    (1, 'SALES','Sales',            'Sales & Business Development');

-- ── Demo Designations ──
INSERT INTO designations (company_id, name, level) VALUES
    (1, 'Chief Executive Officer', 1),
    (1, 'Chief Technology Officer', 2),
    (1, 'Vice President', 3),
    (1, 'Director', 4),
    (1, 'Senior Manager', 5),
    (1, 'Manager', 6),
    (1, 'Team Lead', 7),
    (1, 'Senior Engineer', 8),
    (1, 'Software Engineer', 9),
    (1, 'Junior Engineer', 10),
    (1, 'Intern', 11),
    (1, 'HR Manager', 6),
    (1, 'HR Executive', 8),
    (1, 'Finance Manager', 6),
    (1, 'Accountant', 8);

-- ── Demo Job Grades ──
INSERT INTO job_grades (company_id, code, name, level, min_salary, max_salary) VALUES
    (1, 'L1',  'Band 1 — Executive',   1, 3000000.00, 10000000.00),
    (1, 'L2',  'Band 2 — Director',    2, 2000000.00,  4000000.00),
    (1, 'L3',  'Band 3 — Manager',     3, 1200000.00,  2500000.00),
    (1, 'L4',  'Band 4 — Lead',        4,  800000.00,  1500000.00),
    (1, 'L5',  'Band 5 — Senior',      5,  500000.00,  1000000.00),
    (1, 'L6',  'Band 6 — Associate',   6,  300000.00,   600000.00),
    (1, 'L7',  'Band 7 — Entry',       7,  150000.00,   350000.00);

-- ── Demo Admin User ──
-- Password: Admin@123 (BCrypt cost 12)
-- MUST be changed on first login
INSERT INTO users (company_id, email, password_hash, display_name, is_active, must_change_password)
VALUES (1, 'admin@priyex.com',
        '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS',
        'System Administrator', TRUE, TRUE);

-- Assign SUPER_ADMIN role
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id
FROM users u, roles r
WHERE u.email = 'admin@priyex.com' AND r.name = 'SUPER_ADMIN';
