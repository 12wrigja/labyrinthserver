create table players (id serial primary key, username varchar(16), password varchar(255), currency int, isDev boolean);
create table heroes (id serial primary key, classname varchar(30), attack int, defense int, health int, vision int, movement int);
create table hero_player (player_id int not null, hero_id int not null, level int not null default 1, constraint pk_player_id foreign key (player_id) references players (id) on update cascade on delete cascade, constraint pk_hero_id foreign key (hero_id) references heroes (id) on update cascade on delete cascade);
