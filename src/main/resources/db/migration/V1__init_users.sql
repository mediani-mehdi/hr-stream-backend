CREATE TABLE IF NOT EXISTS "users" (
    id VARCHAR(36) PRIMARY KEY,
    firstname VARCHAR(255),
    lastname VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50)
);

-- Seed a dev admin user (password: Admin#1234)
-- BCrypt for "Admin#1234" (generated once for dev convenience)
INSERT INTO "users" (id, firstname, lastname, email, password, role)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Admin',
    'User',
    'admin@hrstream.local',
    '$2a$10$y2P2EXeL1xG9Y7cYvQ0H1uU2yG8cW6k0wWl0e1i6xwqO2hGm8kM6e',
    'ADMIN'
)
ON CONFLICT (email) DO NOTHING;

