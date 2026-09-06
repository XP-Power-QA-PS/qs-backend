CREATE TABLE floors (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP
);

CREATE TABLE equipments (
    id BIGINT PRIMARY KEY,
    equipment_code VARCHAR(100) NOT NULL UNIQUE,
    equipment_name VARCHAR(200) NOT NULL,
    floor_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    CONSTRAINT fk_equipment_floor FOREIGN KEY (floor_id) REFERENCES floors(id)
);

CREATE TABLE equipment_test_records (
    id BIGINT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    test_month INT NOT NULL,
    test_year INT NOT NULL,
    tested_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    CONSTRAINT fk_test_equipment FOREIGN KEY (equipment_id) REFERENCES equipments(id),
    CONSTRAINT uk_test_month_year UNIQUE (equipment_id, test_month, test_year)
);

-- Insert sample data
INSERT INTO floors (id, name, description, created_at, created_by, updated_at)
VALUES 
(1, 'GO/NOGO 1', 'Main Production Area', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 'GO/NOGO 2', 'R&D Area', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

INSERT INTO equipments (id, equipment_code, equipment_name, floor_id, created_at, created_by, updated_at)
VALUES 
(1, 'EQ-001', 'Voltage Tester A1', 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 'EQ-002', 'Power Meter P2', 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 'EQ-003', 'Spectrum Analyzer S3', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
