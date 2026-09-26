-- ═══════════════════════════════════════════════════════════════════════════
-- V2: Users, Roles, Permissions & Authentication
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Users ──
CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          REFERENCES companies(id),
    employee_id     BIGINT,
    email           VARCHAR(200)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    display_name    VARCHAR(150)    NOT NULL,
    avatar_url      VARCHAR(500),
    phone           VARCHAR(20),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    is_locked       BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_login_attempts INT       NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    last_login_at   TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ,
    must_change_password BOOLEAN    NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    version         INT             NOT NULL DEFAULT 1
);

-- ── Roles ──
CREATE TABLE roles (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          REFERENCES companies(id),
    name            VARCHAR(50)     NOT NULL,
    display_name    VARCHAR(100)    NOT NULL,
    description     VARCHAR(500),
    is_system       BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    UNIQUE (company_id, name)
);

-- ── Permissions ──
CREATE TABLE permissions (
    id              BIGSERIAL       PRIMARY KEY,
    module          VARCHAR(50)     NOT NULL,
    code            VARCHAR(100)    NOT NULL UNIQUE,
    display_name    VARCHAR(150)    NOT NULL,
    description     VARCHAR(500),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Role-Permission mapping ──
CREATE TABLE role_permissions (
    id              BIGSERIAL       PRIMARY KEY,
    role_id         BIGINT          NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id   BIGINT          NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE (role_id, permission_id)
);

-- ── User-Role mapping ──
CREATE TABLE user_roles (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id         BIGINT          NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    assigned_by     BIGINT,
    UNIQUE (user_id, role_id)
);

-- ── User scopes (data access scope: self, department, branch, company, explicit) ──
CREATE TABLE user_scopes (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scope_type      VARCHAR(30)     NOT NULL, -- SELF, DEPARTMENT, BRANCH, COMPANY, CUSTOM
    scope_entity_id BIGINT,                   -- department_id, branch_id, etc.
    permission_id   BIGINT          REFERENCES permissions(id),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT
);

-- ── Refresh Tokens ──
CREATE TABLE refresh_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255)    NOT NULL UNIQUE,
    device_info     VARCHAR(200),
    ip_address      VARCHAR(45),
    expires_at      TIMESTAMPTZ     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    revoked_at      TIMESTAMPTZ
);

-- ── Login Audit ──
CREATE TABLE login_audit (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          REFERENCES users(id),
    email           VARCHAR(200)    NOT NULL,
    action          VARCHAR(30)     NOT NULL, -- LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, TOKEN_REFRESH
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    details         VARCHAR(500),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Password Reset Tokens ──
CREATE TABLE password_reset_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255)    NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Indexes ──
CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_users_employee ON users(employee_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_login_audit_user ON login_audit(user_id);
CREATE INDEX idx_login_audit_created ON login_audit(created_at);
CREATE INDEX idx_password_reset_hash ON password_reset_tokens(token_hash);

-- ═══════════════════════════════════════════════════════════════════════════
-- System Roles & Permissions Seed (system-level, not company-specific)
-- ═══════════════════════════════════════════════════════════════════════════

-- System Roles (company_id NULL = platform-level)
INSERT INTO roles (company_id, name, display_name, description, is_system) VALUES
    (NULL, 'SUPER_ADMIN',   'Super Administrator',  'Full platform access',                          TRUE),
    (NULL, 'HR_ADMIN',      'HR Administrator',     'HR operations and employee management',         TRUE),
    (NULL, 'HR_EXECUTIVE',  'HR Executive',         'Day-to-day HR operations',                      TRUE),
    (NULL, 'MANAGER',       'Manager',              'Team management and approvals',                 TRUE),
    (NULL, 'EMPLOYEE',      'Employee',             'Self-service access',                           TRUE),
    (NULL, 'PAYROLL_ADMIN', 'Payroll Administrator','Compensation and payroll management',           TRUE),
    (NULL, 'FINANCE',       'Finance',              'Financial review and approvals',                TRUE),
    (NULL, 'RECRUITER',     'Recruiter',            'Recruitment and hiring',                        TRUE),
    (NULL, 'TRAINER',       'Trainer',              'Training and learning management',              TRUE),
    (NULL, 'AUDITOR',       'Auditor',              'Read-only audit access',                        TRUE);

-- Core Permissions
INSERT INTO permissions (module, code, display_name, description) VALUES
    -- Auth
    ('AUTH', 'auth.manage_users',          'Manage Users',                'Create, edit, deactivate users'),
    ('AUTH', 'auth.manage_roles',          'Manage Roles',                'Create, edit roles and assign permissions'),
    ('AUTH', 'auth.view_audit_logs',       'View Audit Logs',             'View authentication audit trail'),

    -- Organization
    ('ORG', 'org.manage_company',          'Manage Company',              'Edit company settings and configuration'),
    ('ORG', 'org.manage_branches',         'Manage Branches',             'Create, edit branches and locations'),
    ('ORG', 'org.manage_departments',      'Manage Departments',          'Create, edit departments and teams'),
    ('ORG', 'org.manage_designations',     'Manage Designations',         'Manage designations and job grades'),
    ('ORG', 'org.view_org_chart',          'View Org Chart',              'View organization hierarchy'),

    -- Employees
    ('EMP', 'emp.create',                  'Create Employee',             'Create new employee records'),
    ('EMP', 'emp.edit',                    'Edit Employee',               'Edit employee profiles'),
    ('EMP', 'emp.view_all',               'View All Employees',          'View all employee records'),
    ('EMP', 'emp.view_team',              'View Team Employees',         'View direct report employees'),
    ('EMP', 'emp.view_sensitive',          'View Sensitive Fields',       'View bank, tax, salary fields'),
    ('EMP', 'emp.manage_documents',        'Manage Documents',            'Upload and manage employee documents'),
    ('EMP', 'emp.manage_lifecycle',        'Manage Lifecycle',            'Handle employee status transitions'),

    -- Leave
    ('LEAVE', 'leave.manage_policies',     'Manage Leave Policies',       'Configure leave types and policies'),
    ('LEAVE', 'leave.apply',               'Apply Leave',                 'Submit leave requests'),
    ('LEAVE', 'leave.approve',             'Approve Leave',               'Approve or reject leave requests'),
    ('LEAVE', 'leave.view_team',           'View Team Leave',             'View team leave calendar'),
    ('LEAVE', 'leave.adjust_balance',      'Adjust Leave Balance',        'Manual leave balance adjustments'),
    ('LEAVE', 'leave.manage_holidays',     'Manage Holidays',             'Configure company holidays'),

    -- Attendance
    ('ATTENDANCE', 'att.check_in',         'Check In/Out',                'Record attendance'),
    ('ATTENDANCE', 'att.view_team',        'View Team Attendance',        'View team attendance records'),
    ('ATTENDANCE', 'att.manage_shifts',    'Manage Shifts',               'Configure shift schedules'),
    ('ATTENDANCE', 'att.approve_corrections', 'Approve Corrections',      'Approve attendance corrections'),
    ('ATTENDANCE', 'att.manage_timesheets', 'Manage Timesheets',          'Submit and manage timesheets'),

    -- Payroll
    ('PAYROLL', 'pay.manage_structures',   'Manage Salary Structures',    'Configure salary components'),
    ('PAYROLL', 'pay.assign_salary',       'Assign Salary',               'Assign salary to employees'),
    ('PAYROLL', 'pay.run_payroll',         'Run Payroll',                 'Execute payroll calculations'),
    ('PAYROLL', 'pay.approve_payroll',     'Approve Payroll',             'Approve payroll runs'),
    ('PAYROLL', 'pay.view_payslips',       'View Payslips',               'View payslip details'),

    -- Recruitment
    ('RECRUIT', 'rec.manage_requisitions', 'Manage Requisitions',         'Create and manage job requisitions'),
    ('RECRUIT', 'rec.manage_candidates',   'Manage Candidates',           'Manage candidate pipeline'),
    ('RECRUIT', 'rec.manage_interviews',   'Manage Interviews',           'Schedule and manage interviews'),
    ('RECRUIT', 'rec.manage_offers',       'Manage Offers',               'Create and approve offers'),

    -- Expenses
    ('EXPENSE', 'exp.submit',              'Submit Expenses',             'Submit expense claims'),
    ('EXPENSE', 'exp.approve',             'Approve Expenses',            'Approve expense claims'),
    ('EXPENSE', 'exp.manage_policies',     'Manage Expense Policies',     'Configure expense policies'),

    -- Assets
    ('ASSET', 'asset.manage',              'Manage Assets',               'Manage asset inventory'),
    ('ASSET', 'asset.assign',              'Assign Assets',               'Assign assets to employees'),

    -- Performance
    ('PERF', 'perf.manage_cycles',         'Manage Review Cycles',        'Configure performance cycles'),
    ('PERF', 'perf.submit_review',         'Submit Reviews',              'Submit performance reviews'),
    ('PERF', 'perf.view_team_reviews',     'View Team Reviews',           'View team performance data'),

    -- Training
    ('TRAINING', 'train.manage_courses',   'Manage Courses',              'Create and manage training courses'),
    ('TRAINING', 'train.manage_sessions',  'Manage Sessions',             'Schedule training sessions'),
    ('TRAINING', 'train.enroll',           'Enroll in Training',          'Enroll in training programs'),

    -- Reports
    ('REPORT', 'report.hr',                'HR Reports',                  'Access HR reports'),
    ('REPORT', 'report.payroll',           'Payroll Reports',             'Access payroll reports'),
    ('REPORT', 'report.attendance',        'Attendance Reports',          'Access attendance reports'),
    ('REPORT', 'report.export',            'Export Data',                 'Export reports to CSV/Excel/PDF'),

    -- Admin
    ('ADMIN', 'admin.system_settings',     'System Settings',             'Manage system configuration'),
    ('ADMIN', 'admin.audit_trail',         'Full Audit Trail',            'View complete audit history'),
    ('ADMIN', 'admin.manage_jobs',         'Manage Scheduled Jobs',       'View and manage background jobs');

-- Assign all permissions to SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN';
