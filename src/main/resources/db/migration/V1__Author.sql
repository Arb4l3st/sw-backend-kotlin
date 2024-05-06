create table IF NOT EXISTS author
(
    id          serial primary key,
    full_name   text not null,
    create_date text not null
);