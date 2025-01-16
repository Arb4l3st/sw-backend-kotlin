create table author
(
    id      serial primary key,
    fio     varchar(128) not null,
    created timestamp default current_timestamp
);