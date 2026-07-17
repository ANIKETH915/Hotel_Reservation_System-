-- Wipe operational demo/seed data. Keeps admins and app_settings.
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM payments;
DELETE FROM bookings;
DELETE FROM customers;
DELETE FROM rooms;
SET FOREIGN_KEY_CHECKS = 1;
