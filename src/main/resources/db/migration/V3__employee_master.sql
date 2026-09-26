-- ═══════════════════════════════════════════════════════════════════════════
-- V3: Employee Master & Lifecycle
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Employees ──
CREATE TABLE employees (
    id                  BIGSERIAL       PRIMARY KEY,
    company_id          BIGINT          NOT NULL REFERENCES companies(id),
    employee_code       VARCHAR(30)     NOT NULL,
    user_id             BIGINT          REFERENCES users(id),

    -- Personal
    first_name          VARCHAR(100)    NOT NULL,
    middle_name         VARCHAR(100),
    last_name           VARCHAR(100)    NOT NULL,
    date_of_birth       DATE,
    gender              VARCHAR(20),
    marital_status      VARCHAR(20),
    blood_group         VARCHAR(5),
    nationality         VARCHAR(50)     DEFAULT 'Indian',
    photo_url           VARCHAR(500),

    -- Contact
    personal_email      VARCHAR(200),
    work_email          VARCHAR(200),
    personal_phone      VARCHAR(20),
    work_phone          VARCHAR(20),

    -- Employment
    department_id       BIGINT          REFERENCES departments(id),
    designation_id      BIGINT          REFERENCES designations(id),
    branch_id           BIGINT          REFERENCES branches(id),
    location_id         BIGINT          REFERENCES work_locations(id),
    team_id             BIGINT          REFERENCES teams(id),
    job_grade_id        BIGINT          REFERENCES job_grades(id),
    cost_center_id      BIGINT          REFERENCES cost_centers(id),
    reporting_manager_id BIGINT         REFERENCES employees(id),

    employment_type     VARCHAR(30)     NOT NULL DEFAULT 'FULL_TIME',  -- FULL_TIME, PART_TIME, CONTRACT, INTERN, CONSULTANT
    joining_date        DATE            NOT NULL,
    confirmation_date   DATE,
    probation_end_date  DATE,
    notice_period_days  INT             DEFAULT 30,
    contract_end_date   DATE,

    -- Status
    status              VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',     -- DRAFT, PREBOARDING, ACTIVE, PROBATION, NOTICE, SUSPENDED, EXITED, ARCHIVED
    exit_date           DATE,
    exit_reason         VARCHAR(200),

    -- Metadata
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    version             INT             NOT NULL DEFAULT 1,
    UNIQUE (company_id, employee_code)
);

-- ── Employee Addresses ──
CREATE TABLE employee_addresses (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    address_type    VARCHAR(20)     NOT NULL, -- PERMANENT, CURRENT, EMERGENCY
    address_line1   VARCHAR(200)    NOT NULL,
    address_line2   VARCHAR(200),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(100)    DEFAULT 'India',
    postal_code     VARCHAR(20),
    is_primary      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Emergency Contacts ──
CREATE TABLE employee_contacts (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    contact_name    VARCHAR(150)    NOT NULL,
    relationship    VARCHAR(50)     NOT NULL,
    phone           VARCHAR(20)     NOT NULL,
    email           VARCHAR(200),
    address         VARCHAR(500),
    is_primary      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Dependents ──
CREATE TABLE employee_dependents (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    name            VARCHAR(150)    NOT NULL,
    relationship    VARCHAR(50)     NOT NULL,
    date_of_birth   DATE,
    gender          VARCHAR(20),
    is_disabled     BOOLEAN         DEFAULT FALSE,
    id_number       VARCHAR(50),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Bank Details (sensitive — least privilege) ──
CREATE TABLE employee_bank_details (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    bank_name       VARCHAR(150)    NOT NULL,
    branch_name     VARCHAR(150),
    account_number  VARCHAR(50)     NOT NULL,
    ifsc_code       VARCHAR(20),
    account_type    VARCHAR(30)     DEFAULT 'SAVINGS',
    is_primary      BOOLEAN         NOT NULL DEFAULT TRUE,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (employee_id, account_number)
);

-- ── Tax / Statutory Details (sensitive) ──
CREATE TABLE employee_tax_details (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    pan_number      VARCHAR(20),
    aadhaar_number  VARCHAR(20),
    uan_number      VARCHAR(30),
    pf_number       VARCHAR(30),
    esi_number      VARCHAR(30),
    tax_regime      VARCHAR(20)     DEFAULT 'NEW',  -- OLD, NEW
    effective_from  DATE            NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Employment History (transfers, promotions, status changes) ──
CREATE TABLE employee_employment_history (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    event_type      VARCHAR(50)     NOT NULL, -- HIRED, PROMOTED, TRANSFERRED, DESIGNATION_CHANGE, DEPARTMENT_CHANGE, STATUS_CHANGE, CONFIRMED, SEPARATED
    event_date      DATE            NOT NULL,
    from_value      VARCHAR(200),
    to_value        VARCHAR(200),
    remarks         VARCHAR(500),
    effective_from  DATE            NOT NULL,
    effective_to    DATE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT
);

-- ── Employee Documents ──
CREATE TABLE employee_documents (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    document_type   VARCHAR(50)     NOT NULL, -- RESUME, ID_PROOF, ADDRESS_PROOF, EDUCATION, EXPERIENCE, OFFER_LETTER, CONTRACT, OTHER
    document_name   VARCHAR(200)    NOT NULL,
    file_key        VARCHAR(255)    NOT NULL, -- random storage key, never path
    file_size       BIGINT,
    mime_type       VARCHAR(100),
    expiry_date     DATE,
    is_verified     BOOLEAN         NOT NULL DEFAULT FALSE,
    verified_by     BIGINT,
    verified_at     TIMESTAMPTZ,
    remarks         VARCHAR(500),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Indexes ──
CREATE INDEX idx_employees_company ON employees(company_id);
CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_designation ON employees(designation_id);
CREATE INDEX idx_employees_branch ON employees(branch_id);
CREATE INDEX idx_employees_manager ON employees(reporting_manager_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_user ON employees(user_id);
CREATE INDEX idx_employees_code ON employees(company_id, employee_code);
CREATE INDEX idx_emp_addresses_employee ON employee_addresses(employee_id);
CREATE INDEX idx_emp_contacts_employee ON employee_contacts(employee_id);
CREATE INDEX idx_emp_bank_employee ON employee_bank_details(employee_id);
CREATE INDEX idx_emp_tax_employee ON employee_tax_details(employee_id);
CREATE INDEX idx_emp_history_employee ON employee_employment_history(employee_id);
CREATE INDEX idx_emp_documents_employee ON employee_documents(employee_id);

-- Link departments.head_employee_id and teams.lead_employee_id now that employees table exists
ALTER TABLE departments ADD CONSTRAINT fk_dept_head FOREIGN KEY (head_employee_id) REFERENCES employees(id);
ALTER TABLE teams ADD CONSTRAINT fk_team_lead FOREIGN KEY (lead_employee_id) REFERENCES employees(id);
ALTER TABLE users ADD CONSTRAINT fk_user_employee FOREIGN KEY (employee_id) REFERENCES employees(id);
