-- ═══════════════════════════════════════════════════════════════════════════
-- V19: Clean up all non-core users and reset standard credentials to Admin@123
-- Retains only:
--   1. admin@priyex.com    (Role: SUPER_ADMIN)
--   2. hr@priyex.com       (Role: HR_ADMIN)
--   3. employee@priyex.com (Role: EMPLOYEE)
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Unlink & clean audit logs, notifications, files, and chat messages for non-core users
DELETE FROM audit_logs 
WHERE user_id IS NOT NULL 
  AND user_id NOT IN (SELECT id FROM users WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com'));

DELETE FROM notifications 
WHERE user_id NOT IN (SELECT id FROM users WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com'));

DELETE FROM announcement_acknowledgments 
WHERE user_id NOT IN (SELECT id FROM users WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com'));

DELETE FROM live_chat_messages 
WHERE sender_user_id IS NOT NULL 
  AND sender_user_id NOT IN (SELECT id FROM users WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com'));

UPDATE files 
SET uploaded_by = (SELECT id FROM users WHERE email = 'admin@priyex.com' LIMIT 1) 
WHERE uploaded_by IS NOT NULL 
  AND uploaded_by NOT IN (SELECT id FROM users WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com'));

UPDATE announcements 
SET created_by = (SELECT id FROM users WHERE email = 'admin@priyex.com' LIMIT 1) 
WHERE created_by NOT IN (SELECT id FROM users WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com'));

UPDATE hr_queries 
SET assigned_hr_id = (SELECT id FROM users WHERE email = 'hr@priyex.com' LIMIT 1) 
WHERE assigned_hr_id IS NOT NULL 
  AND assigned_hr_id NOT IN (SELECT id FROM users WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com'));

UPDATE employee_profile_requests 
SET reviewer_id = (SELECT id FROM users WHERE email = 'hr@priyex.com' LIMIT 1) 
WHERE reviewer_id IS NOT NULL 
  AND reviewer_id NOT IN (SELECT id FROM users WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com'));

-- 2. Clean child references for non-core employees (preserve only core employees: 1, 2, 3)
DELETE FROM employee_payslips WHERE employee_id NOT IN (1, 2, 3);
DELETE FROM employee_investment_declarations WHERE employee_id NOT IN (1, 2, 3);
DELETE FROM leave_requests WHERE employee_id IS NOT NULL AND employee_id NOT IN (1, 2, 3);
DELETE FROM hr_queries WHERE employee_id NOT IN (1, 2, 3);
DELETE FROM employee_profile_requests WHERE employee_id NOT IN (1, 2, 3);

UPDATE departments 
SET head_employee_id = 1 
WHERE head_employee_id IS NOT NULL AND head_employee_id NOT IN (1, 2, 3);

UPDATE teams 
SET lead_employee_id = 1 
WHERE lead_employee_id IS NOT NULL AND lead_employee_id NOT IN (1, 2, 3);

UPDATE employees 
SET reporting_manager_id = 2 
WHERE reporting_manager_id IS NOT NULL AND reporting_manager_id NOT IN (1, 2, 3);

-- 3. Break circular foreign key links before deleting records
UPDATE users 
SET employee_id = NULL 
WHERE email NOT IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com');

UPDATE employees 
SET user_id = NULL 
WHERE id NOT IN (1, 2, 3);

-- 4. Delete non-core users (CASCADE will automatically clean user_roles, refresh_tokens, user_sessions)
DELETE FROM users 
WHERE email NOT IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com');

-- 5. Delete non-core employees (CASCADE will automatically clean documents, bank details, contacts, skills)
DELETE FROM employees 
WHERE id NOT IN (1, 2, 3);

-- Reset sequence IDs
SELECT setval('employees_id_seq', (SELECT COALESCE(MAX(id), 3) FROM employees));
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 3) FROM users));

-- 6. Ensure the 3 core users exist with default password Admin@123
-- BCrypt cost 12 hash for 'Admin@123'
UPDATE users 
SET password_hash = '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS',
    must_change_password = FALSE,
    is_active = TRUE,
    failed_login_attempts = 0,
    locked_until = NULL
WHERE email IN ('admin@priyex.com', 'hr@priyex.com', 'employee@priyex.com');

-- 7. Ensure correct roles are assigned to the 3 core users
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id
FROM users u, roles r
WHERE u.email = 'admin@priyex.com' AND r.name = 'SUPER_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id
FROM users u, roles r
WHERE u.email = 'hr@priyex.com' AND r.name = 'HR_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id
FROM users u, roles r
WHERE u.email = 'employee@priyex.com' AND r.name = 'EMPLOYEE'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 8. Relink core users to employee records
UPDATE users SET employee_id = 1 WHERE email = 'employee@priyex.com';
UPDATE users SET employee_id = 2 WHERE email = 'hr@priyex.com';
UPDATE users SET employee_id = 3 WHERE email = 'admin@priyex.com';

UPDATE employees SET user_id = (SELECT id FROM users WHERE email = 'employee@priyex.com') WHERE id = 1;
UPDATE employees SET user_id = (SELECT id FROM users WHERE email = 'hr@priyex.com') WHERE id = 2;
UPDATE employees SET user_id = (SELECT id FROM users WHERE email = 'admin@priyex.com') WHERE id = 3;
