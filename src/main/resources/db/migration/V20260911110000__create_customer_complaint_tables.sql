-- Create customer_complaints table
CREATE TABLE IF NOT EXISTS customer_complaints (
    id BIGINT PRIMARY KEY,
    tracking_no VARCHAR(30) NOT NULL UNIQUE,
    control_no VARCHAR(50),
    building_stage VARCHAR(50),
    capa_no VARCHAR(50),
    year INT NOT NULL,
    month VARCHAR(20) NOT NULL,
    week INT,
    received_date DATE NOT NULL,
    due_date DATE,
    closure_date DATE,
    origin_of_complaint VARCHAR(100),
    internal_external VARCHAR(20) NOT NULL DEFAULT 'EXTERNAL',
    salesforce_capa VARCHAR(100),
    area VARCHAR(100),
    customer_name VARCHAR(150) NOT NULL,
    customer_finding TEXT,
    model VARCHAR(100) NOT NULL,
    issue_description TEXT NOT NULL,
    defect_category VARCHAR(100),
    defect_name VARCHAR(100),
    quantity INT,
    serial_numbers TEXT,
    picture_urls TEXT,
    root_cause TEXT,
    containment_action TEXT,
    containment_due_date DATE,
    corrective_preventive_action TEXT,
    action_owner VARCHAR(100),
    action_due_date DATE,
    action_status VARCHAR(50) DEFAULT 'OPEN',
    status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
    final_status VARCHAR(50),
    remarks TEXT,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create complaint_meetings table for tracking meeting invitations
CREATE TABLE IF NOT EXISTS complaint_meetings (
    id BIGINT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    tracking_no VARCHAR(30) NOT NULL,
    meeting_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME,
    room_location VARCHAR(255) NOT NULL,
    agenda TEXT NOT NULL,
    organizer_email VARCHAR(150) NOT NULL,
    organizer_name VARCHAR(150),
    recipients_json TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_complaint_meetings_complaint FOREIGN KEY (complaint_id) REFERENCES customer_complaints(id)
);

-- Indexes for efficient querying and annual tracking
CREATE INDEX IF NOT EXISTS idx_customer_complaints_year ON customer_complaints(year);
CREATE INDEX IF NOT EXISTS idx_customer_complaints_status ON customer_complaints(status);
CREATE INDEX IF NOT EXISTS idx_customer_complaints_received_date ON customer_complaints(received_date);
CREATE INDEX IF NOT EXISTS idx_complaint_meetings_complaint_id ON complaint_meetings(complaint_id);
