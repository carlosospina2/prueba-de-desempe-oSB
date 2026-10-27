CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    document VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    sede VARCHAR(100) NOT NULL,
    area VARCHAR(100) NOT NULL,
    status VARCHAR(10) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE technicians (
    id BIGSERIAL PRIMARY KEY,
    document VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    salary NUMERIC(15,2) NOT NULL,
    affiliation_date DATE NOT NULL,
    status VARCHAR(10) NOT NULL,
    sede VARCHAR(100) NOT NULL,
    support_profile VARCHAR(500)
);

CREATE TABLE technician_specialties (
    technician_id BIGINT NOT NULL REFERENCES technicians(id),
    specialty VARCHAR(20) NOT NULL,
    PRIMARY KEY (technician_id, specialty)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(20) NOT NULL,
    priority VARCHAR(10) NOT NULL,
    sede VARCHAR(100) NOT NULL,
    affected_asset VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sla_deadline TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL,
    technician_id BIGINT REFERENCES technicians(id)
);
