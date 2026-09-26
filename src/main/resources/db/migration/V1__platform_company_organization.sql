-- ═══════════════════════════════════════════════════════════════════════════
-- V1: Platform, Company & Organization Structure
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Companies ──
CREATE TABLE companies (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(20)     NOT NULL UNIQUE,
    legal_name      VARCHAR(200)    NOT NULL,
    brand_name      VARCHAR(100),
    industry        VARCHAR(100),
    timezone        VARCHAR(50)     NOT NULL DEFAULT 'Asia/Kolkata',
    currency_code   VARCHAR(3)      NOT NULL DEFAULT 'INR',
    locale          VARCHAR(10)     NOT NULL DEFAULT 'en-IN',
    registration_no VARCHAR(100),
    tax_id          VARCHAR(100),
    logo_url        VARCHAR(500),
    website         VARCHAR(200),
    phone           VARCHAR(20),
    email           VARCHAR(150),
    address_line1   VARCHAR(200),
    address_line2   VARCHAR(200),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(100)    DEFAULT 'India',
    postal_code     VARCHAR(20),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    version         INT             NOT NULL DEFAULT 1
);

-- ── Company Settings (key-value per company) ──
CREATE TABLE company_settings (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    setting_key     VARCHAR(100)    NOT NULL,
    setting_value   TEXT,
    description     VARCHAR(500),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (company_id, setting_key)
);

-- ── Branches ──
CREATE TABLE branches (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    code            VARCHAR(20)     NOT NULL,
    name            VARCHAR(150)    NOT NULL,
    address_line1   VARCHAR(200),
    address_line2   VARCHAR(200),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(100)    DEFAULT 'India',
    postal_code     VARCHAR(20),
    phone           VARCHAR(20),
    email           VARCHAR(150),
    timezone        VARCHAR(50),
    is_head_office  BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    UNIQUE (company_id, code)
);

-- ── Work Locations ──
CREATE TABLE work_locations (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    branch_id       BIGINT          REFERENCES branches(id),
    name            VARCHAR(150)    NOT NULL,
    address         VARCHAR(500),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(100),
    latitude        NUMERIC(10,7),
    longitude       NUMERIC(10,7),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT
);

-- ── Departments ──
CREATE TABLE departments (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    parent_id       BIGINT          REFERENCES departments(id),
    code            VARCHAR(20)     NOT NULL,
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(500),
    head_employee_id BIGINT,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    UNIQUE (company_id, code)
);

-- ── Teams ──
CREATE TABLE teams (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    department_id   BIGINT          NOT NULL REFERENCES departments(id),
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(500),
    lead_employee_id BIGINT,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT
);

-- ── Designations ──
CREATE TABLE designations (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    name            VARCHAR(150)    NOT NULL,
    level           INT,
    description     VARCHAR(500),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    UNIQUE (company_id, name)
);

-- ── Job Grades ──
CREATE TABLE job_grades (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    code            VARCHAR(20)     NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    level           INT             NOT NULL,
    min_salary      NUMERIC(15,2),
    max_salary      NUMERIC(15,2),
    description     VARCHAR(500),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    UNIQUE (company_id, code)
);

-- ── Cost Centers ──
CREATE TABLE cost_centers (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    code            VARCHAR(30)     NOT NULL,
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(500),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    UNIQUE (company_id, code)
);

-- ── Indexes ──
CREATE INDEX idx_branches_company ON branches(company_id);
CREATE INDEX idx_departments_company ON departments(company_id);
CREATE INDEX idx_departments_parent ON departments(parent_id);
CREATE INDEX idx_teams_department ON teams(department_id);
CREATE INDEX idx_designations_company ON designations(company_id);
CREATE INDEX idx_job_grades_company ON job_grades(company_id);
CREATE INDEX idx_cost_centers_company ON cost_centers(company_id);
CREATE INDEX idx_work_locations_company ON work_locations(company_id);
CREATE INDEX idx_company_settings_company ON company_settings(company_id);
