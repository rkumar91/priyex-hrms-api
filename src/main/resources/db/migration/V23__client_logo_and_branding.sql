-- ============================================================================
-- V23: Client Logo and Branding Support
-- ============================================================================

-- 1. Alter companies.logo_url to TEXT to support both image URLs and base64 data URIs
ALTER TABLE companies ALTER COLUMN logo_url TYPE TEXT;

-- 2. Seed clean, modern SVG Data URI logos for existing seeded client organizations
-- Priyex Technologies (ID 1)
UPDATE companies 
SET logo_url = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="pgrad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%231e3a8a"/><stop offset="50%" stop-color="%232563eb"/><stop offset="100%" stop-color="%2338bdf8"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(%23pgrad)"/><path d="M32 24 H56 C68 24 74 30 74 42 C74 54 68 60 56 60 H44 V76 H32 Z M44 36 V48 H54 C60 48 62 46 62 42 C62 38 60 36 54 36 Z" fill="white"/></svg>'
WHERE id = 1;

-- Tata Consultancy Services (TCS) (ID 2)
UPDATE companies 
SET logo_url = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="tcsgrad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%230f172a"/><stop offset="100%" stop-color="%231e293b"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(%23tcsgrad)"/><text x="50" y="62" font-family="Arial, Helvetica, sans-serif" font-size="34" font-weight="900" fill="%2338bdf8" text-anchor="middle" letter-spacing="1">TCS</text><circle cx="82" cy="30" r="4" fill="%2338bdf8"/></svg>'
WHERE id = 2;

-- Wipro Limited (ID 3)
UPDATE companies 
SET logo_url = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="wiprograd" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%2314532d"/><stop offset="50%" stop-color="%2315803d"/><stop offset="100%" stop-color="%2322c55e"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(%23wiprograd)"/><text x="50" y="62" font-family="Arial, Helvetica, sans-serif" font-size="24" font-weight="900" fill="white" text-anchor="middle" letter-spacing="0.5">WIPRO</text><circle cx="50" cy="28" r="6" fill="%23facc15"/></svg>'
WHERE id = 3;

-- Infosys Limited (ID 4)
UPDATE companies 
SET logo_url = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="infygrad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%230284c7"/><stop offset="100%" stop-color="%230369a1"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(%23infygrad)"/><text x="50" y="60" font-family="Arial, Helvetica, sans-serif" font-size="20" font-weight="900" fill="white" text-anchor="middle" letter-spacing="0.5">INFOSYS</text><path d="M25 70 Q50 78 75 70" stroke="%2338bdf8" stroke-width="4" fill="none" stroke-linecap="round"/></svg>'
WHERE id = 4;
