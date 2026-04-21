-- Create Database
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

-- Create Roles Table
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Users Table with role_id
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create Audit Logs Table
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    details VARCHAR(500),
    ip_address VARCHAR(45),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_role_id ON users(role_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- Insert Roles
INSERT INTO roles (role_name, description) VALUES ('ADMIN', 'Administrator with full access');
INSERT INTO roles (role_name, description) VALUES ('MODERATOR', 'Moderator with limited admin access');
INSERT INTO roles (role_name, description) VALUES ('USER', 'Regular user with basic access');

-- Insert dummy users for testing
-- 'admin' : 'password123' -> ef92b778ba7158395a409d567709d2d7758708f4c093caee91275d3e269151cc
-- 'user1' : 'mypassword'  -> 89e01536ac207279409d4de1e5253e01f4a1769e6b2229f3f4c664b971a8f98c
-- 'moderator1' : 'modpass123' -> 8847f8a45cfb3c4b7a8b8c8b8c8b8c8b (placeholder hash)

INSERT INTO users (username, password, role_id) VALUES ('admin', 'ef92b778ba7158395a409d567709d2d7758708f4c093caee91275d3e269151cc', 1);
INSERT INTO users (username, password, role_id) VALUES ('user1', '89e01536ac207279409d4de1e5253e01f4a1769e6b2229f3f4c664b971a8f98c', 3);
INSERT INTO users (username, password, role_id) VALUES ('moderator1', 'ef92b778ba7158395a409d567709d2d7758708f4c093caee91275d3e269151cc', 2);
