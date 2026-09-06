CREATE TABLE equipment_daily_tests (
    id BIGINT PRIMARY KEY,
    record_id BIGINT NOT NULL REFERENCES equipment_test_records(id),
    test_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (record_id, test_date)
);

CREATE TABLE equipment_test_attempts (
    id BIGINT PRIMARY KEY,
    daily_test_id BIGINT NOT NULL REFERENCES equipment_daily_tests(id),
    tester_id BIGINT NOT NULL REFERENCES user_accounts(id),
    attempt_time TIMESTAMP WITH TIME ZONE NOT NULL,
    program_status VARCHAR(10) NOT NULL,
    go_status VARCHAR(10) NOT NULL,
    no_go_status VARCHAR(10) NOT NULL,
    result_status VARCHAR(10) NOT NULL,
    remark TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    deleted_at TIMESTAMP
);

