CREATE TABLE IF NOT EXISTS candidate (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    niveau_etude VARCHAR(255),
    domaine_expertise VARCHAR(255),
    experience_professionnelle TEXT
);

