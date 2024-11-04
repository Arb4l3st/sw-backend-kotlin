create table author
(
    id             serial primary key,
    family_name     text not null,
    given_name      text not null,
    patronymic     text not null,
    date_of_create TIMESTAMP NOT NULL DEFAULT now()
);