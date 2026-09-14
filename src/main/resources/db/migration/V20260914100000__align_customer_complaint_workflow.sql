-- Migration to align customer complaints table with 8-step workflow specification
ALTER TABLE customer_complaints
    -- Step 2: Assignment & Priority
    ADD COLUMN IF NOT EXISTS assigned_team VARCHAR(100),
    ADD COLUMN IF NOT EXISTS assigned_person VARCHAR(100),
    ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'MEDIUM',
    ADD COLUMN IF NOT EXISTS assignment_deadline DATE,

    -- Step 3: Containment metadata
    ADD COLUMN IF NOT EXISTS containment_owner VARCHAR(100),
    ADD COLUMN IF NOT EXISTS containment_completion_date DATE,
    ADD COLUMN IF NOT EXISTS containment_status VARCHAR(50) DEFAULT 'IN_PROGRESS',

    -- Step 4: Root Cause Analysis metadata
    ADD COLUMN IF NOT EXISTS root_cause_category VARCHAR(100),
    ADD COLUMN IF NOT EXISTS root_cause_owner VARCHAR(100),
    ADD COLUMN IF NOT EXISTS root_cause_completion_date DATE,

    -- Step 5 & 6: CAPA detailed actions & actual completion
    ADD COLUMN IF NOT EXISTS corrective_action TEXT,
    ADD COLUMN IF NOT EXISTS preventive_action TEXT,
    ADD COLUMN IF NOT EXISTS capa_completion_date DATE,

    -- Step 7: 30-Day Effectiveness Verification
    ADD COLUMN IF NOT EXISTS effectiveness_status VARCHAR(50) DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS effectiveness_verified_date DATE,
    ADD COLUMN IF NOT EXISTS effectiveness_verified_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS effectiveness_remarks TEXT,

    -- Step 8: Closure & Final Evidence
    ADD COLUMN IF NOT EXISTS final_evidence TEXT;

-- Index for priority and assigned team
CREATE INDEX IF NOT EXISTS idx_customer_complaints_priority ON customer_complaints(priority);
CREATE INDEX IF NOT EXISTS idx_customer_complaints_assigned_team ON customer_complaints(assigned_team);
CREATE INDEX IF NOT EXISTS idx_customer_complaints_effectiveness_status ON customer_complaints(effectiveness_status);
