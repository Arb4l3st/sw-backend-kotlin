ALTER TABLE budget
ADD COLUMN author_id INT,
ADD CONSTRAINT FK_author_id FOREIGN KEY (author_id) REFERENCES author(id);