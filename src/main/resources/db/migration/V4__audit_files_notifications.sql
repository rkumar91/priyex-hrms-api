-- ═══════════════════════════════════════════════════════════════════════════
-- V4: Audit Logs, File Metadata, Notifications & System Settings
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Audit Logs ──
CREATE TABLE audit_logs (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          REFERENCES companies(id),
    user_id         BIGINT,
    user_email      VARCHAR(200),
    action          VARCHAR(50)     NOT NULL,  -- CREATE, UPDATE, DELETE, VIEW, EXPORT, APPROVE, REJECT, LOGIN, STATUS_CHANGE
    entity_type     VARCHAR(100)    NOT NULL,  -- Employee, LeaveRequest, PayrollRun, etc.
    entity_id       BIGINT,
    description     VARCHAR(1000),
    old_value       TEXT,   -- JSON of changed fields (redacted for sensitive)
    new_value       TEXT,   -- JSON of changed fields (redacted for sensitive)
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    correlation_id  VARCHAR(50),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── File Metadata ──
CREATE TABLE file_metadata (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          REFERENCES companies(id),
    file_key        VARCHAR(255)    NOT NULL UNIQUE,
    original_name   VARCHAR(300)    NOT NULL,
    mime_type       VARCHAR(100)    NOT NULL,
    file_size       BIGINT          NOT NULL,
    storage_type    VARCHAR(20)     NOT NULL DEFAULT 'LOCAL', -- LOCAL, S3, AZURE_BLOB
    entity_type     VARCHAR(100),
    entity_id       BIGINT,
    uploaded_by     BIGINT          REFERENCES users(id),
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Notification Templates ──
CREATE TABLE notification_templates (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(100)    NOT NULL UNIQUE,
    title_template  VARCHAR(300)    NOT NULL,
    body_template   TEXT            NOT NULL,
    channel         VARCHAR(20)     NOT NULL DEFAULT 'IN_APP', -- IN_APP, EMAIL, BOTH
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Notifications ──
CREATE TABLE notifications (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          REFERENCES companies(id),
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    template_code   VARCHAR(100),
    title           VARCHAR(300)    NOT NULL,
    body            TEXT,
    link            VARCHAR(500),
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    entity_type     VARCHAR(100),
    entity_id       BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Email Delivery Logs ──
CREATE TABLE email_delivery_logs (
    id              BIGSERIAL       PRIMARY KEY,
    recipient_email VARCHAR(200)    NOT NULL,
    subject         VARCHAR(300)    NOT NULL,
    template_code   VARCHAR(100),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED, BOUNCED
    error_message   VARCHAR(500),
    retry_count     INT             NOT NULL DEFAULT 0,
    max_retries     INT             NOT NULL DEFAULT 3,
    next_retry_at   TIMESTAMPTZ,
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Announcements ──
CREATE TABLE announcements (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    title           VARCHAR(300)    NOT NULL,
    body            TEXT            NOT NULL,
    priority        VARCHAR(20)     NOT NULL DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    target_audience VARCHAR(50)     NOT NULL DEFAULT 'ALL',    -- ALL, DEPARTMENT, BRANCH, ROLE
    target_entity_id BIGINT,
    published_at    TIMESTAMPTZ,
    expires_at      TIMESTAMPTZ,
    is_published    BOOLEAN         NOT NULL DEFAULT FALSE,
    requires_ack    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_by      BIGINT          NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Announcement Acknowledgments ──
CREATE TABLE announcement_acknowledgments (
    id              BIGSERIAL       PRIMARY KEY,
    announcement_id BIGINT          NOT NULL REFERENCES announcements(id) ON DELETE CASCADE,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    acknowledged_at TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (announcement_id, user_id)
);

-- ── System Settings ──
CREATE TABLE system_settings (
    id              BIGSERIAL       PRIMARY KEY,
    setting_key     VARCHAR(100)    NOT NULL UNIQUE,
    setting_value   TEXT,
    value_type      VARCHAR(20)     NOT NULL DEFAULT 'STRING', -- STRING, NUMBER, BOOLEAN, JSON
    description     VARCHAR(500),
    is_editable     BOOLEAN         NOT NULL DEFAULT TRUE,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_by      BIGINT
);

-- ── Scheduled Job Runs ──
CREATE TABLE scheduled_job_runs (
    id              BIGSERIAL       PRIMARY KEY,
    job_name        VARCHAR(100)    NOT NULL,
    run_id          VARCHAR(50)     NOT NULL UNIQUE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'RUNNING', -- RUNNING, COMPLETED, FAILED
    started_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ,
    records_processed INT           DEFAULT 0,
    records_failed  INT             DEFAULT 0,
    error_summary   VARCHAR(1000),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Indexes ──
CREATE INDEX idx_audit_logs_company ON audit_logs(company_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_file_metadata_key ON file_metadata(file_key);
CREATE INDEX idx_file_metadata_entity ON file_metadata(entity_type, entity_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read);
CREATE INDEX idx_email_logs_status ON email_delivery_logs(status);
CREATE INDEX idx_announcements_company ON announcements(company_id);
CREATE INDEX idx_announcements_published ON announcements(is_published, published_at);
CREATE INDEX idx_scheduled_jobs_name ON scheduled_job_runs(job_name);
CREATE INDEX idx_scheduled_jobs_status ON scheduled_job_runs(status);
