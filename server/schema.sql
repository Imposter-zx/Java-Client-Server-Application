-- Create Database
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

-- Create Users Table (Resetting to fix column length)
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Insert dummy users for testing (Passwords are hashed with SHA-256)
-- 'admin' : 'password123' -> ef92b778ba7158395a409d567709d2d7758708f4c093caee91275d3e269151cc
-- 'user1' : 'mypassword'  -> 89e01536ac207279409d4de1e5253e01f4a1769e6b2229f3f4c664b971a8f98c

INSERT INTO users (username, password) VALUES ('admin', 'ef92b778ba7158395a409d567709d2d7758708f4c093caee91275d3e269151cc');
INSERT INTO users (username, password) VALUES ('user1', '89e01536ac207279409d4de1e5253e01f4a1769e6b2229f3f4c664b971a8f98c');
