-- Migration: Update floors and add Floor 3 with its equipments
-- Version: 20260907155000

-- Ensure serial_number column exists
ALTER TABLE equipments ADD COLUMN IF NOT EXISTS serial_number VARCHAR(100);

-- 1. Remove Floor 1 and any lingering test records / equipments if present
DELETE FROM equipment_test_attempts 
WHERE daily_test_id IN (
    SELECT dt.id FROM equipment_daily_tests dt 
    JOIN equipment_test_records tr ON dt.record_id = tr.id 
    JOIN equipments e ON tr.equipment_id = e.id 
    WHERE e.floor_id = 1
);

DELETE FROM equipment_daily_tests 
WHERE record_id IN (
    SELECT tr.id FROM equipment_test_records tr 
    JOIN equipments e ON tr.equipment_id = e.id 
    WHERE e.floor_id = 1
);

DELETE FROM equipment_test_records 
WHERE equipment_id IN (
    SELECT id FROM equipments WHERE floor_id = 1
);

DELETE FROM equipments WHERE floor_id = 1;

DELETE FROM floors WHERE id = 1;

-- 2. Ensure Floor 2 exists and is named '2nd Floor'
INSERT INTO floors (id, name, description, created_at, created_by, updated_at)
VALUES (2, '2nd Floor', 'Production Area - 2nd Floor', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE 
SET name = EXCLUDED.name, 
    description = EXCLUDED.description, 
    updated_at = CURRENT_TIMESTAMP;

-- Remove old dummy equipment for Floor 2 (id = 3 or EQ-003) and its test records if present
DELETE FROM equipment_test_attempts 
WHERE daily_test_id IN (
    SELECT dt.id FROM equipment_daily_tests dt 
    JOIN equipment_test_records tr ON dt.record_id = tr.id 
    JOIN equipments e ON tr.equipment_id = e.id 
    WHERE e.floor_id = 2 AND (e.id = 3 OR e.equipment_code = 'EQ-003')
);

DELETE FROM equipment_daily_tests 
WHERE record_id IN (
    SELECT tr.id FROM equipment_test_records tr 
    JOIN equipments e ON tr.equipment_id = e.id 
    WHERE e.floor_id = 2 AND (e.id = 3 OR e.equipment_code = 'EQ-003')
);

DELETE FROM equipment_test_records 
WHERE equipment_id IN (
    SELECT id FROM equipments WHERE floor_id = 2 AND (id = 3 OR equipment_code = 'EQ-003')
);

DELETE FROM equipments WHERE floor_id = 2 AND (id = 3 OR equipment_code = 'EQ-003');

-- Insert or update equipments for Floor 2
INSERT INTO equipments (id, equipment_code, equipment_name, serial_number, floor_id, created_at, created_by, updated_at)
VALUES 
(201, '300003920', 'Hipot tester', '1670866', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(202, '300001977', 'Hipot tester', '1591808', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(203, '300001908', 'Hipot tester', '1670718', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(204, '300001969', 'Hipot tester', '1580880', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(205, '300001976', 'Hipot tester', '1670865', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(206, '300003919', 'Hipot tester', '1591810', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(207, '300001903', 'Hipot tester', '1670719', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(208, '300001925', 'Hipot tester', '1580595', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(209, '300011053', 'Hipot tester', '1670672', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(210, '300001893', 'Hipot tester', '1581037', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(211, '300001913', 'Hipot tester', '1580854', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(212, '300001894', 'Hipot tester', '1670675', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(213, '300001904', 'Hipot tester', '1450545', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(214, '300003654', 'Hipot tester', '1670720', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(215, '300003971', 'Hipot tester', '1591808', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(216, '300001973', 'Hipot tester', '1670867', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(217, '300001959', 'Hipot tester', '1580594', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE 
SET equipment_code = EXCLUDED.equipment_code,
    equipment_name = EXCLUDED.equipment_name,
    serial_number = EXCLUDED.serial_number,
    floor_id = EXCLUDED.floor_id,
    updated_at = CURRENT_TIMESTAMP;

-- 3. Ensure Floor 3 exists and is named '3rd Floor'
INSERT INTO floors (id, name, description, created_at, created_by, updated_at)
VALUES (3, '3rd Floor', 'Production Area - 3rd Floor', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE 
SET name = EXCLUDED.name, 
    description = EXCLUDED.description, 
    updated_at = CURRENT_TIMESTAMP;

-- 4. Insert or update equipments for Floor 3
INSERT INTO equipments (id, equipment_code, equipment_name, serial_number, floor_id, created_at, created_by, updated_at)
VALUES 
(101, '300003592', 'Transformer Analyzer', 'DG11000107', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(102, '300003594', 'Transformer Analyzer', 'DG11000110', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(103, '300003601', 'Transformer Analyzer', 'DG11000056', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(104, '300003674', 'Transformer Analyzer', 'YK5239D07312', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(105, '300003675', 'Transformer Analyzer', 'YK5239C11223', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(106, '300003690', 'Transformer Analyzer', 'YK5239D07323', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(107, '300003695', 'Transformer Analyzer', 'YK5239D07354', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(108, '300001949', 'Automatic Transformer Test System', 'A257170101', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(109, '300001948', 'Automatic Transformer Test System', 'A257170112', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(110, '300001953', 'Automatic Transformer Test System', 'A19A160152', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(111, '300001952', 'Automatic Transformer Test System', 'A19A160166', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(112, '300001951', 'Automatic Transformer Test System', 'A19A160167', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(113, '300011434', 'Automatic Transformer Test System', 'A191250501', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(114, '300011435', 'Automatic Transformer Test System', 'A191250113', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(115, '300011436', 'Automatic Transformer Test System', 'A191250534', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(116, '300011437', 'Automatic Transformer Test System', 'A191250543', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(117, '300011547', 'Transformer comprehensive tester', 'N/A', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(118, '300011548', 'Transformer comprehensive tester', 'N/A', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(119, '300011549', 'Transformer comprehensive tester', 'N/A', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(120, '300011550', 'Transformer comprehensive tester', 'N/A', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(121, '300011910', 'Automatic Transformer Test System', 'A19C250305', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(122, '300011911', 'Automatic Transformer Test System', 'A19A250145', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(123, '300011912', 'Automatic Transformer Test System', 'A19A250139', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(124, '300011913', 'Automatic Transformer Test System', 'A19A250142', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(125, '300012141', 'Automatic Transformer Test System', 'A192260120', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(126, '300012142', 'Automatic Transformer Test System', 'A192260155', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(127, '300003606', 'Hipot tester', 'AA1905X01932', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(128, '300003607', 'Hipot tester', 'AA1905X01933', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(129, '300003604', 'Hipot tester', 'AA1905X01928', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(130, '300003628', 'Hipot tester', 'AA1905X02278', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(131, '300003665', 'Hipot tester', 'AA1905X02472', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(132, '300003666', 'Hipot tester', 'AA1905X02477', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(133, '300003667', 'Hipot tester', 'AA1905X02468', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(134, '300003684', 'Hipot tester', 'AA1905X02639', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(135, '300003686', 'Hipot tester', 'AA1905X02633', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(136, '300003688', 'Hipot tester', 'AA1905X02636', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(137, '300003689', 'Hipot tester', 'AA1905X02637', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(138, '300003707', 'Hipot tester', '1704164013GFG', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(139, '300003709', 'Hipot tester', '1704164012GFG', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(140, '300003712', 'Hipot tester', '1704164015GFG', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(141, '300003710', 'Hipot tester', '1704164011GFG', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(142, '300003711', 'Hipot tester', '1704164016GFG', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(143, '300003708', 'Hipot tester', '1704164014GFG', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(144, '300000594', 'Hipot tester', 'N086170113', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(145, '300004348', 'Hipot tester', 'B206163030FG', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(146, '300012171', 'Hipot tester', 'B6061630314F', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(147, '300012172', 'Hipot tester', 'B6061630315F', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(148, '300012173', 'Hipot tester', 'B6061630316F', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(149, '300012174', 'Hipot tester', 'B6061630317F', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE 
SET equipment_code = EXCLUDED.equipment_code,
    equipment_name = EXCLUDED.equipment_name,
    serial_number = EXCLUDED.serial_number,
    floor_id = EXCLUDED.floor_id,
    updated_at = CURRENT_TIMESTAMP;
