-- ═══════════════════════════════════════════════════════════════════════════
-- V13: Live HR Helpdesk Chat Pool & Message Threads
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS hr_query_messages (
    id              BIGSERIAL       PRIMARY KEY,
    query_id        BIGINT          NOT NULL REFERENCES hr_queries(id) ON DELETE CASCADE,
    sender_user_id  BIGINT          REFERENCES users(id),
    sender_type     VARCHAR(20)     NOT NULL, -- 'EMPLOYEE' or 'HR'
    sender_name     VARCHAR(100)    NOT NULL,
    message_text    TEXT            NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_hr_query_messages_query ON hr_query_messages(query_id);
CREATE INDEX IF NOT EXISTS idx_hr_query_messages_created ON hr_query_messages(created_at);

-- Populate existing query messages so prior history is preserved in chat format
INSERT INTO hr_query_messages (query_id, sender_user_id, sender_type, sender_name, message_text, created_at)
SELECT q.id, NULL, 'EMPLOYEE', (e.first_name || ' ' || e.last_name), q.message, q.created_at
FROM hr_queries q
JOIN employees e ON e.id = q.employee_id
WHERE NOT EXISTS (SELECT 1 FROM hr_query_messages m WHERE m.query_id = q.id);

INSERT INTO hr_query_messages (query_id, sender_user_id, sender_type, sender_name, message_text, created_at)
SELECT q.id, q.assigned_hr_id, 'HR', COALESCE(u.display_name, 'HR Specialist'), q.hr_response, COALESCE(q.responded_at, q.updated_at)
FROM hr_queries q
LEFT JOIN users u ON u.id = q.assigned_hr_id
WHERE q.hr_response IS NOT NULL AND TRIM(q.hr_response) != ''
  AND NOT EXISTS (
      SELECT 1 FROM hr_query_messages m 
      WHERE m.query_id = q.id AND m.sender_type = 'HR'
  );
