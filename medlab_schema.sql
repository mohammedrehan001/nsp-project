-- =========================================================================
-- Medical Laboratory Management System (MedLab)
-- Database Creation & Schema Setup Script for MySQL Workbench
-- Developer: Mohammad Rehan
-- Database: medlab_db
-- =========================================================================

-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS medlab_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medlab_db;

-- =========================================================================
-- Table: users (Authentication & Role-based Access)
-- =========================================================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'Admin',
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Default Admin and Staff Accounts
INSERT INTO users (username, email, password, role, full_name) VALUES
('admin', 'admin@poornima.org', 'admin', 'Admin', 'Mohammad Rehan (Administrator)'),
('staff', 'staff@poornima.org', 'staff', 'Staff', 'Laboratory Technician (Staff)');

-- =========================================================================
-- Table: instruments (Laboratory Diagnostic Equipment)
-- =========================================================================
DROP TABLE IF EXISTS instruments;
CREATE TABLE instruments (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'Lab Instruments',
    manufacturer VARCHAR(150) NOT NULL,
    model VARCHAR(100) NOT NULL,
    serial_no VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    location VARCHAR(150) NOT NULL,
    purchase_date VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Operational',
    last_maintenance VARCHAR(50) NOT NULL,
    next_maintenance VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Laboratory Instruments Data
INSERT INTO instruments (id, name, category, manufacturer, model, serial_no, quantity, location, purchase_date, status, last_maintenance, next_maintenance) VALUES
('INS-101', 'Microscope', 'Lab Instruments', 'Olympus', 'CX23-LED', 'SN-OLY-4412', 4, 'Pathology Lab Room 102', '15-Jan-2024', 'Operational', '10-Aug-2026', '10-Feb-2027'),
('INS-102', 'Spectrophotometer', 'Lab Instruments', 'Thermo Scientific', 'Evolution 201', 'SN-TS-9921', 2, 'Biochemistry Station', '12-Mar-2023', 'Operational', '05-Jul-2026', '05-Jan-2027'),
('INS-103', 'Centrifuge', 'Lab Instruments', 'Beckman Coulter', 'Allegra X-30R', 'SN-BC-7723', 3, 'Sample Prep Area', '20-Jun-2023', 'Operational', '18-Aug-2026', '18-Feb-2027'),
('INS-104', 'Hematology Analyzer', 'Lab Instruments', 'Sysmex Corporation', 'XN-550', 'SN-SYX-3310', 1, 'Hematology Main Bay', '10-Nov-2023', 'Calibration Required', '01-Jun-2026', '01-Dec-2026'),
('INS-105', 'Biochemistry Analyzer', 'Lab Instruments', 'Roche Cobas', 'Cobas c311', 'SN-RCH-8120', 1, 'Biochemistry Section', '05-May-2022', 'Operational', '12-Sep-2026', '12-Mar-2027'),
('INS-106', 'Incubator', 'Lab Instruments', 'Memmert', 'IN55', 'SN-MEM-6641', 2, 'Microbiology Room', '18-Aug-2024', 'Operational', '15-Jul-2026', '15-Jan-2027'),
('INS-107', 'Autoclave', 'Lab Instruments', 'Equitron', 'EQ-PAD-75', 'SN-EQ-5529', 2, 'Sterilization & Wash Bay', '22-Feb-2024', 'Under Maintenance', '20-Aug-2026', '20-Nov-2026'),
('INS-108', 'pH Meter', 'Lab Instruments', 'Mettler Toledo', 'SevenDirect SD20', 'SN-MT-1194', 3, 'Reagent Prep Bench', '14-Oct-2024', 'Operational', '25-Aug-2026', '25-Feb-2027'),
('INS-109', 'Micropipette Set', 'Lab Instruments', 'Eppendorf', 'Research Plus 10-1000uL', 'SN-EPP-9023', 8, 'All Testing Stations', '02-Dec-2024', 'Operational', '10-Sep-2026', '10-Dec-2026');

-- =========================================================================
-- Table: reagents (Medicines, Chemical Reagents & Diagnostic Test Kits)
-- =========================================================================
DROP TABLE IF EXISTS reagents;
CREATE TABLE reagents (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'Medicines / Reagents',
    manufacturer VARCHAR(150) NOT NULL,
    batch_no VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    expiry_date VARCHAR(50) NOT NULL,
    supplier VARCHAR(150) NOT NULL,
    storage_location VARCHAR(150) NOT NULL,
    min_stock INT NOT NULL DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Reagents & Test Kits Data
INSERT INTO reagents (id, name, category, manufacturer, batch_no, quantity, price, expiry_date, supplier, storage_location, min_stock) VALUES
('REA-101', 'Glucose Reagent', 'Medicines / Reagents', 'Roche Diagnostics', 'BTH-GLU-9021', 15, 1450.00, '15-Nov-2027', 'Apex Bio Supplies', 'Cold Storage (2-8°C)', 5),
('REA-102', 'Hemoglobin Reagent', 'Medicines / Reagents', 'Erba Mannheim', 'BTH-HGB-3341', 22, 850.00, '10-Aug-2027', 'MedSupply Corp', 'Room Temp Cabinet', 8),
('REA-103', 'Cholesterol Reagent', 'Medicines / Reagents', 'Siemens Healthineers', 'BTH-CHOL-7782', 8, 2100.00, '05-Dec-2026', 'MediLife Diagnostics', 'Cold Storage (2-8°C)', 6),
('REA-104', 'Urea Reagent', 'Medicines / Reagents', 'Bio-Rad Labs', 'BTH-UREA-4419', 12, 1650.00, '20-Jan-2028', 'Apex Bio Supplies', 'Cold Storage (2-8°C)', 4),
('REA-105', 'Creatinine Reagent', 'Medicines / Reagents', 'Beckman Coulter', 'BTH-CRE-6120', 10, 1250.00, '18-Feb-2028', 'LabWorld Agencies', 'Room Temp Cabinet', 5),
('REA-106', 'Dengue Test Kits', 'Medicines / Reagents', 'Abbott Diagnostics', 'BTH-DNG-5522', 4, 3800.00, '30-Oct-2026', 'Apex Bio Supplies', 'Cold Storage (2-8°C)', 10),
('REA-107', 'Troponin Test Kits', 'Medicines / Reagents', 'Siemens Healthineers', 'BTH-TROP-8811', 3, 4500.00, '15-Jan-2027', 'MediLife Diagnostics', 'Deep Freezer (-20°C)', 5),
('REA-108', 'COVID-19 Test Kits', 'Medicines / Reagents', 'Thermo Fisher', 'BTH-COV-1029', 18, 5200.00, '25-Dec-2027', 'MedSupply Corp', 'Deep Freezer (-20°C)', 6);

-- =========================================================================
-- Table: consumables (Laboratory Disposables & Consumables)
-- =========================================================================
DROP TABLE IF EXISTS consumables;
CREATE TABLE consumables (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'Consumables',
    quantity INT NOT NULL DEFAULT 1,
    unit VARCHAR(50) NOT NULL,
    batch_no VARCHAR(100) NOT NULL,
    expiry_date VARCHAR(50) NOT NULL,
    supplier VARCHAR(150) NOT NULL,
    min_stock INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Consumables Data
INSERT INTO consumables (id, name, category, quantity, unit, batch_no, expiry_date, supplier, min_stock) VALUES
('CON-101', 'Glass Test Tubes', 'Consumables', 250, 'Pieces', 'BTH-TT-1102', '30-Dec-2029', 'Borosil Scientific', 50),
('CON-102', 'Disposable Syringes', 'Consumables', 180, 'Pieces', 'BTH-SYR-9844', '15-Jun-2028', 'HMD DispoVan', 100),
('CON-103', 'Blood Vacutainers', 'Consumables', 320, 'Pieces', 'BTH-VAC-7711', '20-Oct-2027', 'BD Biosciences', 100),
('CON-104', 'Nitrile Gloves', 'Consumables', 8, 'Boxes (100 pcs)', 'BTH-GLV-4432', '15-Aug-2028', 'Safeguard Medical', 15),
('CON-105', 'Surgical Masks', 'Consumables', 12, 'Boxes (50 pcs)', 'BTH-MSK-2210', '10-Nov-2028', 'MediCare Supplies', 10),
('CON-106', 'Hypodermic Needles', 'Consumables', 45, 'Pieces', 'BTH-NDL-6631', '18-Sep-2028', 'HMD DispoVan', 100),
('CON-107', 'Glass Slides', 'Consumables', 15, 'Packs (50 pcs)', 'BTH-SLD-8820', '01-Jan-2030', 'Blue Star Optical', 5),
('CON-108', 'Cotton Wool', 'Consumables', 14, 'Rolls', 'BTH-COT-3319', '25-May-2029', 'Pavitra Pharma', 5),
('CON-109', 'Alcohol Swabs', 'Consumables', 6, 'Boxes (100 pcs)', 'BTH-ALC-5541', '12-Jul-2027', 'Becton Dickinson', 10);

-- =========================================================================
-- Verify Tables and Record Counts
-- =========================================================================
SELECT 'instruments' AS table_name, COUNT(*) AS total_records FROM instruments
UNION ALL
SELECT 'reagents' AS table_name, COUNT(*) AS total_records FROM reagents
UNION ALL
SELECT 'consumables' AS table_name, COUNT(*) AS total_records FROM consumables
UNION ALL
SELECT 'users' AS table_name, COUNT(*) AS total_records FROM users;
