-- Fix seed passwords: all users use Admin1234!
-- BCrypt hash verified at runtime 2026-10-27
UPDATE users
SET password = '$2a$10$gyrcA2eF9AzC8v8kr1IXm.nCED9e5rP8LckwnoSaCqAU2WF8sxA8e'
WHERE email IN ('admin@helpflow.com', 'tecnico@helpflow.com', 'usuario@helpflow.com');
