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
    serial_number VARCHAR(100),
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
(2, '2nd Floor', 'Production Area - 2nd Floor', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

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
(217, '300001959', 'Hipot tester', '1580594', 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
