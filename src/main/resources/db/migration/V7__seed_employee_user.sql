-- ═══════════════════════════════════════════════════════════════════════════
-- V7: Seed Demo Employee Accounts
-- ═══════════════════════════════════════════════════════════════════════════

INSERT INTO users (company_id, email, password_hash, display_name, is_active, must_change_password)
VALUES (1, 'employee@priyex.com',
        '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS',
        'Rajesh Kumar (Employee)', TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id
FROM users u, roles r
WHERE u.email = 'employee@priyex.com' AND r.name = 'EMPLOYEE'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO users (company_id, email, password_hash, display_name, is_active, must_change_password)
VALUES (1, 'hr@priyex.com',
        '$2a$12$Kjp/39zJZFzrRXK517pA2.sEk6sI/0nNarE/v8n.bo4ZZ62ZdgzuS',
        'Priya Sharma (HR Manager)', TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, u.id
FROM users u, roles r
WHERE u.email = 'hr@priyex.com' AND r.name = 'HR_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;
