CREATE TABLE IF NOT EXISTS ingredients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    default_amount DOUBLE PRECISION NOT NULL,
    amount_type VARCHAR(50) NOT NULL
);