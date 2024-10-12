create table budget
(
    id          serial  primary key,
    year        int     not null,
    month       int     not null,
    amount      int     not null,
    authorId    int,
    type        text    not null
);