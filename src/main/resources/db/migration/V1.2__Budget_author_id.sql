alter table budget add column author_id int;

alter table budget add constraint "fk_budget_to_author" FOREIGN KEY (author_id) REFERENCES author on delete set null;

