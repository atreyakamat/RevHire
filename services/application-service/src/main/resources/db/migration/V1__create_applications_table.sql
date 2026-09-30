CREATE TABLE applications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_id BIGINT,
    user_id BIGINT,
    resume_id BIGINT,
    status VARCHAR(255),
    applied_at DATETIME(6),
    updated_at DATETIME(6),

    PRIMARY KEY (id)
);