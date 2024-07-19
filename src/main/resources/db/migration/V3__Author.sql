CREATE TABLE author
(
    id         serial primary key,
    full_name  varchar(255) not null,
    created_at timestamp not null
)