create table author
(
    id         serial primary key,
    full_name  text  not null,
    created_at timestamp not null
);

ALTER TABLE Budget
    ADD COLUMN author_id INT;

ALTER TABLE Budget
    ADD CONSTRAINT fk_budget_author
        FOREIGN KEY (author_id) REFERENCES author(id);
