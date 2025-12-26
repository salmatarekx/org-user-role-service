-- 1) Organization
INSERT INTO organizations (id, name, created_at)
VALUES (1, 'First Default Organization', now());

-- 2) Roles
INSERT INTO roles (id, organization_id, name, created_at)
VALUES
    (1, 1, 'ORG_ADMIN', now()),
    (2, 1, 'ORG_USER', now());

-- 3) Admin user
INSERT INTO users (
    id,
    organization_id,
    email,
    display_name,
    password_hash,
    created_at,
    updated_at
)
VALUES (
           1,
           1,
           'superadmin@org.com',
           'Super Org Admin',
           '$2a$10$gl58HiRb.QukJbDG.faEL.YT64AS55soyqJ7uWR9r8JYU7A3HmEny',
           now(),
           now()
       );

-- 4) Assign role
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1);
