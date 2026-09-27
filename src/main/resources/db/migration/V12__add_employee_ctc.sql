-- ═══════════════════════════════════════════════════════════════════════════
-- V12: Add Annual CTC column to employees table and update seed data
-- ═══════════════════════════════════════════════════════════════════════════

ALTER TABLE employees ADD COLUMN IF NOT EXISTS annual_ctc NUMERIC(15,2) NOT NULL DEFAULT 1200000.00;

-- Update existing demo employees with their configured CTC values
UPDATE employees SET annual_ctc = 2400000.00 WHERE employee_code = 'EMP-1001';
UPDATE employees SET annual_ctc = 1800000.00 WHERE employee_code = 'EMP-1002';
UPDATE employees SET annual_ctc = 1600000.00 WHERE employee_code = 'EMP-1003';
