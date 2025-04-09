-- This file will create the schema for our tests

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(20) UNIQUE
    );

-- Create users table
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    name VARCHAR(100) NOT NULL,
    nik VARCHAR(16) UNIQUE NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

-- Create user_roles table for many-to-many relationship
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id INT NOT NULL,
                                          PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
    );

-- Create pacillians table
CREATE TABLE IF NOT EXISTS pacillians (
                                          user_id BIGINT PRIMARY KEY,
                                          medical_history TEXT,
                                          FOREIGN KEY (user_id) REFERENCES users (id)
    );

-- Create caregivers table
CREATE TABLE IF NOT EXISTS caregivers (
                                          user_id BIGINT PRIMARY KEY,
                                          speciality VARCHAR(255) NOT NULL,
    work_address VARCHAR(255) NOT NULL,
    average_rating DOUBLE DEFAULT 0.0,
    rating_count INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users (id)
    );

-- Create working_schedules table
CREATE TABLE IF NOT EXISTS working_schedules (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 caregiver_id BIGINT NOT NULL,
                                                 day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (caregiver_id) REFERENCES caregivers (user_id)
    );