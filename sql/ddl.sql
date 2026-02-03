CREATE TABLE attach_file (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    uuid       VARCHAR(255)                         NOT NULL,
    name       VARCHAR(255)                         NOT NULL,
    size       BIGINT   DEFAULT 0                   NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP() NULL
);

