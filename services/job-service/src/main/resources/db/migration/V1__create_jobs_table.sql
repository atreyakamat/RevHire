CREATE TABLE jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    skills VARCHAR(255) NOT NULL,
    salary DECIMAL(12,2) NOT NULL,
    job_type ENUM('CONTRACT', 'FULL_TIME', 'INTERNSHIP', 'PART_TIME') NOT NULL,
    status ENUM('ACTIVE', 'CLOSED', 'DRAFT') NOT NULL,
    employer_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),

    PRIMARY KEY (id)
);