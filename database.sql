CREATE DATABASE IF NOT EXISTS secure_file_transfer;
USE secure_file_transfer;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    mobile VARCHAR(20) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    file_data LONGBLOB NOT NULL,
    uploaded_by INT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_time TIMESTAMP NULL,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    file_id INT NOT NULL,
    status ENUM('pending', 'approved', 'rejected', 'expired') NOT NULL DEFAULT 'pending',
    request_key VARCHAR(255) NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_time TIMESTAMP NULL,
    previous_request_id INT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (previous_request_id) REFERENCES requests(id) ON DELETE SET NULL
);

INSERT INTO users (full_name, email, mobile, username, password, role) 
VALUES ('Default Admin', 'admin@system.com', '0000000000', 'admin', '00000', 'admin');
