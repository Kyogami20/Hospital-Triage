CREATE DATABASE IF NOT EXISTS meditriage_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE meditriage_db;

CREATE TABLE IF NOT EXISTS patients (
    id          INT             NOT NULL AUTO_INCREMENT,
    name        VARCHAR(150)    NOT NULL,
    age         INT             NOT NULL,
    symptoms    TEXT            NOT NULL,
    level       TINYINT         NOT NULL COMMENT '1=Resucitacion, 2=Emergente, 3=Urgente, 4=Menos Urgente, 5=No Urgente',
    status      VARCHAR(20)     NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING | ATTENDED',
    arrival_at  DATETIME        NOT NULL,
    attended_at DATETIME        NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    INDEX idx_level_status   (level, status, arrival_at),

    INDEX idx_arrival_at     (arrival_at),

    INDEX idx_name           (name),

    INDEX idx_attended_at    (attended_at),

    CONSTRAINT chk_level CHECK (level BETWEEN 1 AND 5),

    CONSTRAINT chk_status CHECK (status IN ('WAITING', 'ATTENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


SHOW TABLES;
DESCRIBE patients;
