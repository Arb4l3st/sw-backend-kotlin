create table author
(
    id              serial primary key,
    fio             varchar(150)  not null,
    date_of_create  timestamp not null default now()
);