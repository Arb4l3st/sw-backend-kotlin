create table author
(
    id              serial      primary key,
    fio             varchar     not null,
    created_date    timestamp   not null
);