create table author
(
    id                  serial      primary key,
    fio                 text        not null,
    creationDateTime    timestamp   not null
);