-- Admin user (password: Admin1234!)
INSERT INTO users (document, name, email, password, sede, area, status, role)
VALUES ('ADMIN001', 'Administrador HelpFlow', 'admin@helpflow.com',
'$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FXDt.Spec8zW',
'Bogotá', 'TI', 'ACTIVO', 'ROLE_ADMIN');

-- Technician user (password: Tech1234!)
INSERT INTO users (document, name, email, password, sede, area, status, role)
VALUES ('TEC001', 'Carlos Técnico', 'tecnico@helpflow.com',
'$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FXDt.Spec8zW',
'Bogotá', 'Soporte', 'ACTIVO', 'ROLE_TECNICO');

-- Regular user (password: User1234!)
INSERT INTO users (document, name, email, password, sede, area, status, role)
VALUES ('USR001', 'María Usuario', 'usuario@helpflow.com',
'$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FXDt.Spec8zW',
'Medellín', 'Contabilidad', 'ACTIVO', 'ROLE_USUARIO');

INSERT INTO technicians (document, name, salary, affiliation_date, status, sede, support_profile)
VALUES ('TEC001', 'Carlos Técnico', 3500000, '2023-01-15', 'ACTIVO', 'Bogotá', 'Especialista en redes y hardware');

INSERT INTO technician_specialties (technician_id, specialty) VALUES (1, 'RED');
INSERT INTO technician_specialties (technician_id, specialty) VALUES (1, 'HARDWARE');
