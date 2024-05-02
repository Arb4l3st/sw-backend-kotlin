CREATE TABLE IF NOT EXISTS budget
(
    id     SERIAL PRIMARY KEY,
    year   INT  NOT NULL,
    month  INT  NOT NULL,
    amount INT  NOT NULL,
    type   TEXT NOT NULL
);
