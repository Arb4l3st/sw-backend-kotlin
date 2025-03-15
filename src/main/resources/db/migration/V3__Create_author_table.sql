CREATE TABLE IF NOT EXISTS author (
                        id SERIAL PRIMARY KEY,
                        full_name TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT NOW() NOT NULL
);