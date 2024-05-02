create table author (
    id              serial primary key,
    last_name       text not null,
    first_name      text not null,
    father_name     text not null,
    date_of_create  text not null
)