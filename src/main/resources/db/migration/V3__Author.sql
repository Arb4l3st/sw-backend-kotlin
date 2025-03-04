create extension if not exists "uuid-ossp";

create table author
(
    id            uuid         not null primary key default uuid_generate_v4(),
    full_name     varchar(500) not null,
    creation_date timestamp    not null
);

alter table budget add column author_id uuid null references author(id);
