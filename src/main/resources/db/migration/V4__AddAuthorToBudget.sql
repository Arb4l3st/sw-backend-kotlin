alter table budget add author_id int;
alter table budget add foreign key(author_id) references author(id);
