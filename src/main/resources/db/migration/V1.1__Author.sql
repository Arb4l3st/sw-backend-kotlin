create table author
(
    id     serial primary key,
    name   text not null,
    creation_date timestamp
);