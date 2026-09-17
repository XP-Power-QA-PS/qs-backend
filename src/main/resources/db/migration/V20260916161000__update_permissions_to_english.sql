-- Migration: Update permissions name and description to English
-- Version: 20260916161000

-- Module 1: Go/No-Go & Daily Testing (GONOGO)
UPDATE permissions 
SET name = 'View Test Logs', 
    description = 'View daily Go/No-Go inspection logs and historical test records' 
WHERE code = 'GONOGO_VIEW';

UPDATE permissions 
SET name = 'Record Attempt', 
    description = 'Input test parameters and log shift Pass/Fail test attempts' 
WHERE code = 'GONOGO_RECORD';

UPDATE permissions 
SET name = 'Machine Verify', 
    description = 'Verify machine calibration and authenticate test runs' 
WHERE code = 'GONOGO_VERIFY';

UPDATE permissions 
SET name = 'Supervisor Override', 
    description = 'Sign-off on out-of-spec conditions and approve test waivers' 
WHERE code = 'GONOGO_OVERRIDE';

UPDATE permissions 
SET name = 'Export Reports', 
    description = 'Export daily and monthly test records to Excel or PDF' 
WHERE code = 'GONOGO_EXPORT';

-- Module 2: Equipments & Floors (EQUIPMENT)
UPDATE permissions 
SET name = 'View Equipments & Floors', 
    description = 'Browse equipment inventory, test specs, and plant layout' 
WHERE code = 'EQUIPMENT_VIEW';

UPDATE permissions 
SET name = 'Manage Equipment', 
    description = 'Create, configure test programs, or remove machine records' 
WHERE code = 'EQUIPMENT_MANAGE';

UPDATE permissions 
SET name = 'Manage Floors', 
    description = 'Create, update, or reorganize plant floor zones and lines' 
WHERE code = 'FLOOR_MANAGE';

UPDATE permissions 
SET name = 'Calibrate & Status', 
    description = 'Update calibration due dates and operational statuses' 
WHERE code = 'EQUIPMENT_CALIBRATE';

-- Module 3: Customer Complaints (COMPLAINT)
UPDATE permissions 
SET name = 'View Complaints', 
    description = 'Access complaint register and customer defect telemetry' 
WHERE code = 'COMPLAINT_VIEW';

UPDATE permissions 
SET name = 'Create Complaint', 
    description = 'Register incoming customer RMA and defect tickets' 
WHERE code = 'COMPLAINT_CREATE';

UPDATE permissions 
SET name = 'Update 8D Workflow', 
    description = 'Execute root cause analysis (D4) and CAPA corrective actions' 
WHERE code = 'COMPLAINT_UPDATE_8D';

UPDATE permissions 
SET name = 'Approve & Close CAPA', 
    description = 'Sign-off on containment results and officially close cases' 
WHERE code = 'COMPLAINT_APPROVE_CLOSE';

UPDATE permissions 
SET name = 'Export 8D Reports', 
    description = 'Generate customer-facing 8D dossiers and audit summaries' 
WHERE code = 'COMPLAINT_EXPORT';

-- Module 4: CFT Meetings & Email Alerts (COMMUNICATION)
UPDATE permissions 
SET name = 'View Meetings & Emails', 
    description = 'Review CFT meeting calendar and email transmission history' 
WHERE code = 'COMMUNICATION_VIEW';

UPDATE permissions 
SET name = 'Schedule CFT Meeting', 
    description = 'Organize cross-functional problem solving sessions' 
WHERE code = 'COMMUNICATION_SCHEDULE';

UPDATE permissions 
SET name = 'Distribute Minutes', 
    description = 'Broadcast meeting minutes and assigned action items' 
WHERE code = 'COMMUNICATION_SEND_MINUTES';

UPDATE permissions 
SET name = 'Configure Alert Lists', 
    description = 'Manage engineer and supervisor distribution lists' 
WHERE code = 'COMMUNICATION_CONFIG_ALERTS';

-- Module 5: System & Security Administration (SYSTEM_ADMIN)
UPDATE permissions 
SET name = 'View User Directory', 
    description = 'Access staff accounts, employee badges, and status rosters' 
WHERE code = 'ADMIN_VIEW_USERS';

UPDATE permissions 
SET name = 'Manage User Accounts', 
    description = 'Create, update credentials, or deactivate employee accounts' 
WHERE code = 'ADMIN_MANAGE_USERS';

UPDATE permissions 
SET name = 'Reset Default Password', 
    description = 'Quick reset default password for shopfloor line operators' 
WHERE code = 'ADMIN_RESET_PASSWORD';

UPDATE permissions 
SET name = 'Manage Roles', 
    description = 'Create, edit, or adjust role definitions and scopes' 
WHERE code = 'ADMIN_MANAGE_ROLES';

UPDATE permissions 
SET name = 'Configure Permissions', 
    description = 'Manage granular permissions and access control matrix' 
WHERE code = 'ADMIN_CONFIG_PERMISSIONS';
