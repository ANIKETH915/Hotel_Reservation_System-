-- ============================================================
-- Grand Azure Hotel Reservation System
-- Database: hotel_reservation_system
-- MySQL 8+ | CodeAlpha Java Internship Task 4
-- ============================================================

CREATE DATABASE IF NOT EXISTS hotel_reservation_system
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE hotel_reservation_system;

-- ------------------------------------------------------------
-- Admins (authentication)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admins (
    admin_id              INT AUTO_INCREMENT PRIMARY KEY,
    username              VARCHAR(50)  NOT NULL UNIQUE,
    password_hash         VARCHAR(128) NOT NULL,
    salt                  VARCHAR(64)  NOT NULL,
    full_name             VARCHAR(100) NOT NULL,
    security_answer_hash  VARCHAR(128) NOT NULL,
    remember_token_hash   VARCHAR(128) NULL,
    remember_token_expires DATETIME NULL,
    is_active             TINYINT(1)   NOT NULL DEFAULT 1,
    last_login            DATETIME NULL,
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Rooms
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rooms (
    room_id     INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10)  NOT NULL UNIQUE,
    room_type   ENUM('Standard','Deluxe','Suite','Luxury Suite','Presidential Suite') NOT NULL,
    floor       INT          NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    capacity    INT          NOT NULL DEFAULT 2,
    status      ENUM('Available','Booked','Reserved','Maintenance','Cleaning') NOT NULL DEFAULT 'Available',
    image_path  VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_room_status (status),
    INDEX idx_room_type (room_type)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Customers
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(120) NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    phone       VARCHAR(20)  NOT NULL,
    address     TEXT NULL,
    id_proof    VARCHAR(80)  NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_phone (phone),
    INDEX idx_customer_name (full_name)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Bookings
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
    booking_id      INT AUTO_INCREMENT PRIMARY KEY,
    customer_id     INT NOT NULL,
    room_id         INT NOT NULL,
    check_in        DATE NOT NULL,
    check_out       DATE NOT NULL,
    days            INT NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL DEFAULT 0,
    booking_status  ENUM('Confirmed','Checked In','Checked Out','Cancelled') NOT NULL DEFAULT 'Confirmed',
    payment_status  ENUM('Pending','Paid','Refunded','Partial') NOT NULL DEFAULT 'Pending',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id)
        REFERENCES rooms(room_id) ON DELETE CASCADE,
    CONSTRAINT chk_booking_dates CHECK (check_out > check_in),
    INDEX idx_booking_dates (room_id, check_in, check_out),
    INDEX idx_booking_customer (customer_id),
    INDEX idx_booking_status (booking_status),
    INDEX idx_booking_created (created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Payments
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    payment_id      INT AUTO_INCREMENT PRIMARY KEY,
    booking_id      INT NOT NULL,
    payment_method  ENUM('Cash','UPI','Credit Card','Debit Card','Net Banking') NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    payment_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_id  VARCHAR(64) NOT NULL UNIQUE,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id)
        REFERENCES bookings(booking_id) ON DELETE CASCADE,
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_booking (booking_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Application settings
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_settings (
    setting_key   VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

INSERT INTO app_settings (setting_key, setting_value) VALUES
    ('hotel_name', 'Grand Azure Hotel & Suites'),
    ('currency', 'INR'),
    ('tax_rate', '12'),
    ('theme', 'light'),
    ('vip_booking_threshold', '3')
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);

-- ------------------------------------------------------------
-- Seed admin
-- username: admin | password: admin123 | security answer: azure
-- Hash generated by PasswordUtil (PBKDF2) — updated by DatabaseInitializer if needed.
-- Placeholder rows; DatabaseInitializer will ensure correct hash on first run.
-- ------------------------------------------------------------
INSERT INTO admins (username, password_hash, salt, full_name, security_answer_hash)
SELECT 'admin', 'PENDING', 'PENDING', 'System Administrator', 'PENDING'
WHERE NOT EXISTS (SELECT 1 FROM admins WHERE username = 'admin');

-- Operational data (rooms, customers, bookings, payments) is created
-- only through the application UI — no demo/seed records.

