ALTER TABLE budget
    ADD author_id INTEGER,
    ADD CONSTRAINT fk$budget$author_id FOREIGN KEY(author_id) REFERENCES author(id);