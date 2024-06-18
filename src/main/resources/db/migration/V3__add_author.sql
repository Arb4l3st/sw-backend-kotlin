create table author
(
    id         serial primary key,
    full_name  varchar (255) not null,
    date_time  timestamptz default now()
);


alter table budget
    ADD COLUMN author_id INT REFERENCES author (id);








