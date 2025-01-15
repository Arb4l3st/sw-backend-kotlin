create table author
(
    id      serial primary key,
    fio     varchar(64) not null,
    created timestamp
);