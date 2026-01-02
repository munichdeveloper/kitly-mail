CREATE TABLE emails (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_email VARCHAR(255) NOT NULL,
    from_name VARCHAR(255) NOT NULL,
    to_email VARCHAR(255) NOT NULL,
    to_name VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    html_content TEXT,
    text_content TEXT,
    status VARCHAR(50) NOT NULL,
    external_id VARCHAR(255),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

CREATE INDEX idx_emails_status ON emails(status);
CREATE INDEX idx_emails_to_email ON emails(to_email);
CREATE INDEX idx_emails_created_at ON emails(created_at);
