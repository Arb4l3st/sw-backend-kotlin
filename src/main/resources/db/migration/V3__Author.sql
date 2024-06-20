create table author
(
    id            serial primary key,
    full_name     text  not null,
    date_created  timestamp  not null
);