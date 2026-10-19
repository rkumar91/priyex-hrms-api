-- ═══════════════════════════════════════════════════════════════════════════
-- V22: Client Domains, Multi-Tenant Auto-Identification & RBAC Test Matrix
-- ═══════════════════════════════════════════════════════════════════════════

-- ── 1. Create company_domains table for domain-based organization detection ──
CREATE TABLE IF NOT EXISTS company_domains (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    domain_name     VARCHAR(150)    NOT NULL,
    is_primary      BOOLEAN         NOT NULL DEFAULT FALSE,
    is_verified     BOOLEAN         NOT NULL DEFAULT TRUE,
    is_shared       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (company_id, domain_name)
);

CREATE INDEX IF NOT EXISTS idx_company_domains_name ON company_domains(domain_name);
CREATE INDEX IF NOT EXISTS idx_company_domains_company ON company_domains(company_id);

-- ── 2. Seed verified email domains for client organizations ──
INSERT INTO company_domains (company_id, domain_name, is_primary, is_verified, is_shared)
VALUES
    (1, 'priyex.com', TRUE, TRUE, FALSE),
    (1, 'priyex.in', FALSE, TRUE, FALSE),
    (2, 'tcs.com', TRUE, TRUE, FALSE),
    (2, 'tataconsultancy.com', FALSE, TRUE, FALSE),
    (3, 'wipro.com', TRUE, TRUE, FALSE),
    (4, 'infosys.com', TRUE, TRUE, FALSE)
ON CONFLICT (company_id, domain_name) DO UPDATE
SET is_primary = EXCLUDED.is_primary,
    is_verified = EXCLUDED.is_verified;

-- ── 3. Ensure core system roles exist (ADMIN, HR, SUPER_ADMIN, MANAGER, EMPLOYEE) ──
INSERT INTO roles (company_id, name, display_name, description, is_system, is_active)
VALUES
    (NULL, 'ADMIN', 'Organization Administrator', 'Full management access within assigned client organization', TRUE, TRUE),
    (NULL, 'HR', 'HR Manager', 'HR operations, attendance, leaves, and employee records for assigned client', TRUE, TRUE)
ON CONFLICT (company_id, name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    is_active = TRUE;

-- Map full permissions to ADMIN and HR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name IN ('ADMIN', 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name IN ('HR', 'HR_ADMIN')
  AND p.code NOT LIKE 'auth.manage_roles%'
ON CONFLICT DO NOTHING;

-- ── 4. Seed Client Designations ──
INSERT INTO designations (company_id, name, level, description)
VALUES
    -- TCS (Company 2)
    (2, 'Vice President & Head of Delivery', 3, 'Senior Executive'),
    (2, 'Lead HR Business Partner', 5, 'People Operations Lead'),
    (2, 'Technical Delivery Manager', 6, 'Project & Team Management'),
    (2, 'Senior Software Engineer', 8, 'Full Stack Application Engineer'),

    -- Wipro (Company 3)
    (3, 'General Manager & Business Head', 3, 'Business Unit Head'),
    (3, 'Senior Manager - Talent Engagement', 5, 'HR & Talent Operations'),
    (3, 'Cloud Solutions Architect & Manager', 6, 'Cloud Team Manager'),
    (3, 'Cloud Engineer', 8, 'Cloud Infrastructure Engineer'),

    -- Infosys (Company 4)
    (4, 'Development Centre Head', 3, 'Centre Head'),
    (4, 'Lead HR Business Partner', 5, 'Talent Operations Manager'),
    (4, 'Principal Technology Lead', 6, 'Engineering Manager'),
    (4, 'Senior Systems Specialist', 8, 'Systems & Platform Engineer')
ON CONFLICT (company_id, name) DO NOTHING;

-- ── 5. Standard Password Hash: Admin@123 (BCrypt cost 12) ──
-- Hash: $2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS

-- ── 6. Seed Platform Super Admin ──
INSERT INTO users (company_id, email, password_hash, display_name, is_active, must_change_password)
VALUES (1, 'superadmin@priyex.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Platform Super Administrator', TRUE, FALSE)
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    display_name = EXCLUDED.display_name,
    is_active = TRUE;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id
FROM users u, roles r
WHERE u.email = 'superadmin@priyex.com' AND r.name = 'SUPER_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ── 7. Seed Client 1: TCS (Company ID 2) Test Accounts ──
-- Admin, HR, Manager, Employee
INSERT INTO users (company_id, email, password_hash, display_name, is_active, must_change_password)
VALUES 
    (2, 'admin@tcs.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Rajesh Sharma', TRUE, FALSE),
    (2, 'hr@tcs.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Ananya Roy', TRUE, FALSE),
    (2, 'manager@tcs.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Vikram Malhotra', TRUE, FALSE),
    (2, 'employee@tcs.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Pooja Verma', TRUE, FALSE)
ON CONFLICT (email) DO UPDATE
SET company_id = EXCLUDED.company_id,
    password_hash = EXCLUDED.password_hash,
    display_name = EXCLUDED.display_name,
    is_active = TRUE;

-- Assign Roles for TCS
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'admin@tcs.com' AND r.name IN ('ADMIN', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'hr@tcs.com' AND r.name IN ('HR', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'manager@tcs.com' AND r.name = 'MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'employee@tcs.com' AND r.name = 'EMPLOYEE'
ON CONFLICT DO NOTHING;

-- Seed Employee Records for TCS
INSERT INTO employees (company_id, employee_code, first_name, last_name, work_email,
                       department_id, designation_id, branch_id, employment_type, joining_date, status)
VALUES
    (2, 'TCS-001', 'Rajesh', 'Sharma', 'admin@tcs.com',
     (SELECT id FROM departments WHERE company_id = 2 AND code = 'ENG' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 2 AND name = 'Vice President & Head of Delivery' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 2 LIMIT 1),
     'FULL_TIME', '2021-01-15', 'ACTIVE'),
    (2, 'TCS-002', 'Ananya', 'Roy', 'hr@tcs.com',
     (SELECT id FROM departments WHERE company_id = 2 AND code = 'HR' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 2 AND name = 'Lead HR Business Partner' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 2 LIMIT 1),
     'FULL_TIME', '2022-03-01', 'ACTIVE'),
    (2, 'TCS-003', 'Vikram', 'Malhotra', 'manager@tcs.com',
     (SELECT id FROM departments WHERE company_id = 2 AND code = 'ENG' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 2 AND name = 'Technical Delivery Manager' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 2 LIMIT 1),
     'FULL_TIME', '2022-06-15', 'ACTIVE'),
    (2, 'TCS-004', 'Pooja', 'Verma', 'employee@tcs.com',
     (SELECT id FROM departments WHERE company_id = 2 AND code = 'ENG' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 2 AND name = 'Senior Software Engineer' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 2 LIMIT 1),
     'FULL_TIME', '2023-08-01', 'ACTIVE')
ON CONFLICT (company_id, employee_code) DO NOTHING;

-- Link TCS Users and Employees
UPDATE users u
SET employee_id = e.id
FROM employees e
WHERE e.work_email = u.email AND u.company_id = 2;

UPDATE employees e
SET user_id = u.id,
    reporting_manager_id = (SELECT id FROM employees WHERE employee_code = 'TCS-003' LIMIT 1)
FROM users u
WHERE u.email = e.work_email AND e.company_id = 2;

-- Manager reports to Admin
UPDATE employees
SET reporting_manager_id = (SELECT id FROM employees WHERE employee_code = 'TCS-001' LIMIT 1)
WHERE employee_code = 'TCS-003';

-- ── 8. Seed Client 2: WIPRO (Company ID 3) Test Accounts ──
INSERT INTO users (company_id, email, password_hash, display_name, is_active, must_change_password)
VALUES 
    (3, 'admin@wipro.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Suresh Nair', TRUE, FALSE),
    (3, 'hr@wipro.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Kavita Menon', TRUE, FALSE),
    (3, 'manager@wipro.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Arjun Reddy', TRUE, FALSE),
    (3, 'employee@wipro.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Sneha Patel', TRUE, FALSE)
ON CONFLICT (email) DO UPDATE
SET company_id = EXCLUDED.company_id,
    password_hash = EXCLUDED.password_hash,
    display_name = EXCLUDED.display_name,
    is_active = TRUE;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'admin@wipro.com' AND r.name IN ('ADMIN', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'hr@wipro.com' AND r.name IN ('HR', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'manager@wipro.com' AND r.name = 'MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'employee@wipro.com' AND r.name = 'EMPLOYEE'
ON CONFLICT DO NOTHING;

INSERT INTO employees (company_id, employee_code, first_name, last_name, work_email,
                       department_id, designation_id, branch_id, employment_type, joining_date, status)
VALUES
    (3, 'WIP-001', 'Suresh', 'Nair', 'admin@wipro.com',
     (SELECT id FROM departments WHERE company_id = 3 AND code = 'CLOUD' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 3 AND name = 'General Manager & Business Head' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 3 LIMIT 1),
     'FULL_TIME', '2020-04-01', 'ACTIVE'),
    (3, 'WIP-002', 'Kavita', 'Menon', 'hr@wipro.com',
     (SELECT id FROM departments WHERE company_id = 3 AND code = 'HR' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 3 AND name = 'Senior Manager - Talent Engagement' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 3 LIMIT 1),
     'FULL_TIME', '2021-08-15', 'ACTIVE'),
    (3, 'WIP-003', 'Arjun', 'Reddy', 'manager@wipro.com',
     (SELECT id FROM departments WHERE company_id = 3 AND code = 'CLOUD' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 3 AND name = 'Cloud Solutions Architect & Manager' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 3 LIMIT 1),
     'FULL_TIME', '2022-02-10', 'ACTIVE'),
    (3, 'WIP-004', 'Sneha', 'Patel', 'employee@wipro.com',
     (SELECT id FROM departments WHERE company_id = 3 AND code = 'CLOUD' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 3 AND name = 'Cloud Engineer' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 3 LIMIT 1),
     'FULL_TIME', '2023-05-20', 'ACTIVE')
ON CONFLICT (company_id, employee_code) DO NOTHING;

UPDATE users u
SET employee_id = e.id
FROM employees e
WHERE e.work_email = u.email AND u.company_id = 3;

UPDATE employees e
SET user_id = u.id,
    reporting_manager_id = (SELECT id FROM employees WHERE employee_code = 'WIP-003' LIMIT 1)
FROM users u
WHERE u.email = e.work_email AND e.company_id = 3;

UPDATE employees
SET reporting_manager_id = (SELECT id FROM employees WHERE employee_code = 'WIP-001' LIMIT 1)
WHERE employee_code = 'WIP-003';

-- ── 9. Seed Client 3: INFOSYS (Company ID 4) Test Accounts ──
INSERT INTO users (company_id, email, password_hash, display_name, is_active, must_change_password)
VALUES 
    (4, 'admin@infosys.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Deepak Gupta', TRUE, FALSE),
    (4, 'hr@infosys.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Priya Iyer', TRUE, FALSE),
    (4, 'manager@infosys.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Rohan Kulkarni', TRUE, FALSE),
    (4, 'employee@infosys.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Aditi Rao', TRUE, FALSE)
ON CONFLICT (email) DO UPDATE
SET company_id = EXCLUDED.company_id,
    password_hash = EXCLUDED.password_hash,
    display_name = EXCLUDED.display_name,
    is_active = TRUE;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'admin@infosys.com' AND r.name IN ('ADMIN', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'hr@infosys.com' AND r.name IN ('HR', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'manager@infosys.com' AND r.name = 'MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'employee@infosys.com' AND r.name = 'EMPLOYEE'
ON CONFLICT DO NOTHING;

INSERT INTO employees (company_id, employee_code, first_name, last_name, work_email,
                       department_id, designation_id, branch_id, employment_type, joining_date, status)
VALUES
    (4, 'INF-001', 'Deepak', 'Gupta', 'admin@infosys.com',
     (SELECT id FROM departments WHERE company_id = 4 AND code = 'AI-DATA' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 4 AND name = 'Development Centre Head' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 4 LIMIT 1),
     'FULL_TIME', '2019-11-01', 'ACTIVE'),
    (4, 'INF-002', 'Priya', 'Iyer', 'hr@infosys.com',
     (SELECT id FROM departments WHERE company_id = 4 AND code = 'HR' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 4 AND name = 'Lead HR Business Partner' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 4 LIMIT 1),
     'FULL_TIME', '2021-01-18', 'ACTIVE'),
    (4, 'INF-003', 'Rohan', 'Kulkarni', 'manager@infosys.com',
     (SELECT id FROM departments WHERE company_id = 4 AND code = 'AI-DATA' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 4 AND name = 'Principal Technology Lead' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 4 LIMIT 1),
     'FULL_TIME', '2022-07-01', 'ACTIVE'),
    (4, 'INF-004', 'Aditi', 'Rao', 'employee@infosys.com',
     (SELECT id FROM departments WHERE company_id = 4 AND code = 'AI-DATA' LIMIT 1),
     (SELECT id FROM designations WHERE company_id = 4 AND name = 'Senior Systems Specialist' LIMIT 1),
     (SELECT id FROM branches WHERE company_id = 4 LIMIT 1),
     'FULL_TIME', '2023-09-11', 'ACTIVE')
ON CONFLICT (company_id, employee_code) DO NOTHING;

UPDATE users u
SET employee_id = e.id
FROM employees e
WHERE e.work_email = u.email AND u.company_id = 4;

UPDATE employees e
SET user_id = u.id,
    reporting_manager_id = (SELECT id FROM employees WHERE employee_code = 'INF-003' LIMIT 1)
FROM users u
WHERE u.email = e.work_email AND e.company_id = 4;

UPDATE employees
SET reporting_manager_id = (SELECT id FROM employees WHERE employee_code = 'INF-001' LIMIT 1)
WHERE employee_code = 'INF-003';

-- ── 10. Also seed Manager for Company 1 (Priyex) ──
INSERT INTO users (company_id, email, password_hash, display_name, is_active, must_change_password)
VALUES (1, 'manager@priyex.com', '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS', 'Priyex Tech Lead Manager', TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id FROM users u, roles r WHERE u.email = 'manager@priyex.com' AND r.name = 'MANAGER'
ON CONFLICT DO NOTHING;

-- Reset sequences
SELECT setval('employees_id_seq', (SELECT MAX(id) FROM employees));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('company_domains_id_seq', (SELECT COALESCE(MAX(id), 1) FROM company_domains));
