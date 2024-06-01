create table author
(
    id         serial primary key,
    name       varchar(255) not null,
    created_at timestamptz default now()
);