alter table budget
add column author_id int,
add constraint fk_budget_author foreign key (author_id) references author(id);