-- ═══════════════════════════════════════════════════════════════════════════
-- V10: Corporate Employee Master Fields, Address Details & ID Documents
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Complete Address, PF, Banking, Emergency, and Corporate Fields
ALTER TABLE employees ADD COLUMN IF NOT EXISTS address_line2 VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS country VARCHAR(100) DEFAULT 'India';

ALTER TABLE employees ADD COLUMN IF NOT EXISTS permanent_address_line1 VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS permanent_address_line2 VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS permanent_city VARCHAR(100);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS permanent_state VARCHAR(100);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS permanent_postal_code VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS permanent_country VARCHAR(100) DEFAULT 'India';

ALTER TABLE employees ADD COLUMN IF NOT EXISTS esi_number VARCHAR(50);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS pan_number VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS aadhaar_number VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS pf_nominee_name VARCHAR(150);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS pf_nominee_relationship VARCHAR(50);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS pf_joining_date DATE;

ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_name VARCHAR(150);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_branch VARCHAR(150);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_account_type VARCHAR(50) DEFAULT 'SALARY';

ALTER TABLE employees ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(150);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS emergency_contact_relationship VARCHAR(50);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(30);

ALTER TABLE employees ADD COLUMN IF NOT EXISTS work_location VARCHAR(150) DEFAULT 'Bangalore HQ';

-- 2. Enhance employee_documents table with file_data column for cloudless storage
ALTER TABLE employee_documents ADD COLUMN IF NOT EXISTS file_data TEXT;
ALTER TABLE employee_documents ALTER COLUMN file_key DROP NOT NULL;

-- 3. Seed Realistic Corporate Data for Demo Employees
UPDATE employees
SET 
    -- Address
    address_line1 = 'Tower 4, Flat 602, Prestige Park Vista',
    address_line2 = 'Kadubeesanahalli, Outer Ring Road',
    city = 'Bangalore',
    state = 'Karnataka',
    postal_code = '560103',
    country = 'India',
    permanent_address_line1 = 'House No. 42, Civil Lines',
    permanent_address_line2 = 'Near Circuit House',
    permanent_city = 'Prayagraj',
    permanent_state = 'Uttar Pradesh',
    permanent_postal_code = '211001',
    permanent_country = 'India',
    
    -- PF & Statutory
    pf_number = 'UP/NOI/0034182/000/0001001',
    uan_number = '101489201928',
    esi_number = '31-00-123456-000-0001',
    pan_number = 'ABCDE1234F',
    aadhaar_number = 'XXXX-XXXX-9021',
    pf_nominee_name = 'Sunita Sharma',
    pf_nominee_relationship = 'Spouse',
    pf_joining_date = '2023-01-15',
    
    -- Banking
    bank_name = 'HDFC Bank',
    bank_branch = 'Kadubeesanahalli Cyber Park',
    bank_account_number = '5010098234123',
    bank_ifsc = 'HDFC0001234',
    bank_account_type = 'SALARY',
    
    -- Emergency
    emergency_contact_name = 'Sunita Sharma',
    emergency_contact_relationship = 'Spouse',
    emergency_contact_phone = '+91 9876543211',
    
    -- Corporate
    blood_group = 'O+',
    marital_status = 'MARRIED',
    date_of_birth = '1991-08-14',
    work_location = 'Bangalore Tech Park'
WHERE id = 1;

UPDATE employees
SET 
    -- Address
    address_line1 = 'Apt 12B, Sobha Iris',
    address_line2 = 'Bellandur EcoSpace',
    city = 'Bangalore',
    state = 'Karnataka',
    postal_code = '560103',
    country = 'India',
    permanent_address_line1 = 'B-104, Green Glen Layout',
    permanent_address_line2 = 'Near Motherhood Hospital',
    permanent_city = 'Bangalore',
    permanent_state = 'Karnataka',
    permanent_postal_code = '560103',
    permanent_country = 'India',
    
    -- PF & Statutory
    pf_number = 'UP/NOI/0034182/000/0001002',
    uan_number = '101489201929',
    esi_number = '31-00-123456-000-0002',
    pan_number = 'PQRST5678M',
    aadhaar_number = 'XXXX-XXXX-8412',
    pf_nominee_name = 'Vikram Sharma',
    pf_nominee_relationship = 'Spouse',
    pf_joining_date = '2023-03-01',
    
    -- Banking
    bank_name = 'ICICI Bank',
    bank_branch = 'Bellandur Branch',
    bank_account_number = '001205019283',
    bank_ifsc = 'ICIC0000012',
    bank_account_type = 'SALARY',
    
    -- Emergency
    emergency_contact_name = 'Vikram Sharma',
    emergency_contact_relationship = 'Spouse',
    emergency_contact_phone = '+91 9811223344',
    
    -- Corporate
    blood_group = 'B+',
    marital_status = 'MARRIED',
    date_of_birth = '1988-04-22',
    work_location = 'Bangalore Tech Park'
WHERE id = 2;

UPDATE employees
SET 
    -- Address
    address_line1 = 'Flat 303, Palm Grove Heights',
    address_line2 = 'Sector 62',
    city = 'Noida',
    state = 'Uttar Pradesh',
    postal_code = '201309',
    country = 'India',
    permanent_address_line1 = 'House 18, Block C',
    permanent_address_line2 = 'Indirapuram',
    permanent_city = 'Ghaziabad',
    permanent_state = 'Uttar Pradesh',
    permanent_postal_code = '201014',
    permanent_country = 'India',
    
    -- PF & Statutory
    pf_number = 'DL/CPM/0029103/000/0001003',
    uan_number = '101489201930',
    esi_number = '31-00-123456-000-0003',
    pan_number = 'JKLMN9012K',
    aadhaar_number = 'XXXX-XXXX-6190',
    pf_nominee_name = 'Ramesh Verma',
    pf_nominee_relationship = 'Father',
    pf_joining_date = '2023-05-10',
    
    -- Banking
    bank_name = 'Axis Bank',
    bank_branch = 'Sector 62 Noida',
    bank_account_number = '919010045612345',
    bank_ifsc = 'UTIB0000919',
    bank_account_type = 'SALARY',
    
    -- Emergency
    emergency_contact_name = 'Ramesh Verma',
    emergency_contact_relationship = 'Father',
    emergency_contact_phone = '+91 9711224455',
    
    -- Corporate
    blood_group = 'A+',
    marital_status = 'SINGLE',
    date_of_birth = '1995-11-09',
    work_location = 'Noida Innovation Hub'
WHERE id = 3;

-- 4. Seed sample uploaded documents for EMP-1001 (Rajesh Kumar)
INSERT INTO employee_documents (employee_id, document_type, document_name, mime_type, file_size, is_verified, remarks, created_at)
VALUES
    (1, 'PAN_CARD', 'Pan_Card_Rajesh_Kumar.pdf', 'application/pdf', 245000, TRUE, 'Verified by HR against NSDL database', NOW() - INTERVAL '30 days'),
    (1, 'AADHAAR_CARD', 'Aadhaar_Card_Masked.pdf', 'application/pdf', 380000, TRUE, 'UIDAI QR code verified', NOW() - INTERVAL '30 days'),
    (1, 'DEGREE_CERTIFICATE', 'BTech_Degree_Certificate.pdf', 'application/pdf', 1200000, TRUE, 'Original verified at onboarding', NOW() - INTERVAL '25 days'),
    (1, 'PREVIOUS_RELIEVING', 'Previous_Company_Relieving_Letter.pdf', 'application/pdf', 540000, TRUE, 'Service verified', NOW() - INTERVAL '20 days')
ON CONFLICT DO NOTHING;
