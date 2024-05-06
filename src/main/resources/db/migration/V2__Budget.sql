create table IF NOT EXISTS budget
(
    id     serial primary key,
    year   int  not null,
    month  int  not null,
    amount int  not null,
    type   text not null
);

-- changeset1
UPDATE budget
set type = 'Расход'
where type = 'Комиссия';

-- changeset2
ALTER TABLE budget
    ADD COLUMN author_id integer REFERENCES author (id);

