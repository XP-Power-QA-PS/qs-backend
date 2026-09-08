-- Migration: Update floors and add Floor 3 with its equipments
-- Version: 20260907155000

-- 1. Rename Floor 1 & Floor 2 to '1st Floor' and '2nd Floor'
UPDATE floors 
SET name = '1st Floor', 
    updated_at = CURRENT_TIMESTAMP 
WHERE id = 1;

UPDATE floors 
SET name = '2nd Floor', 
    updated_at = CURRENT_TIMESTAMP 
WHERE id = 2;

-- 2. Insert Floor 3 if not exists
INSERT INTO floors (id, name, description, created_at, created_by, updated_at)
VALUES (3, '3rd Floor', 'Production Area - 3rd Floor', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE 
SET name = EXCLUDED.name, 
    description = EXCLUDED.description, 
    updated_at = CURRENT_TIMESTAMP;

-- 3. Insert equipments for Floor 3
INSERT INTO equipments (id, equipment_code, equipment_name, floor_id, created_at, created_by, updated_at)
VALUES 
(101, 'I-AT-001', 'Tranformer Analyzer', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(102, 'I-AT-003', 'Tranformer Analyzer', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(103, 'I-AT-009', 'Tranformer Analyzer', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(104, 'I-AT-025', 'Tranformer Analyzer', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(105, 'I-AT-027', 'Tranformer Analyzer', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(106, 'I-AT-029', 'Tranformer Analyzer', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(107, 'I-AT-035', 'Tranformer Analyzer', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(108, 'I-AT-036', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(109, 'I-AT-037', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(110, 'I-AT-038', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(111, 'I-AT-039', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(112, 'I-AT-040', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(113, 'I-AT-044', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(114, 'I-AT-045', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(115, 'I-AT-046', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(116, 'I-AT-047', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(117, 'I-AT-048', 'Transformer comprehensive tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(118, 'I-AT-049', 'Transformer comprehensive tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(119, 'I-AT-050', 'Transformer comprehensive tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(120, 'I-AT-051', 'Transformer comprehensive tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(121, 'I-AT-052', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(122, 'I-AT-053', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(123, 'I-AT-054', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(124, 'I-AT-055', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(125, 'I-AT-056', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(126, 'I-AT-057', 'Automatic Transformer Test System', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(127, 'I-HP-008', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(128, 'I-HP-009', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(129, 'I-HP-013', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(130, 'I-HP-018', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(131, 'I-HP-022', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(132, 'I-HP-023', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(133, 'I-HP-024', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(134, 'I-HP-030', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(135, 'I-HP-032', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(136, 'I-HP-034', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(137, 'I-HP-035', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(138, 'I-HP-036', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(139, 'I-HP-037', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(140, 'I-HP-038', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(141, 'I-HP-039', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(142, 'I-HP-040', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(143, 'I-HP-041', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(144, 'I-HP-042', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(145, 'I-HP-044', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(146, 'I-HP-047', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(147, 'I-HP-048', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(148, 'I-HP-049', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(149, 'I-HP-050', 'Hipot tester', 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (equipment_code) DO NOTHING;
