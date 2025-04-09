-- This file contains initial data for development purposes

-- Roles (these should be created by the DataInitializer, but are here as a fallback)
INSERT INTO roles (name) VALUES ('ROLE_PACILLIAN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_CAREGIVER') ON CONFLICT (name) DO NOTHING;