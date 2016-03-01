/*
 * Entity Tables
 */
create table rarities (id serial primary key, display_name varchar(30));

create table players (id serial primary key, username varchar(16), password varchar(255), currency int, is_dev boolean);

create table heroes (id serial primary key, class varchar(255) not null, attack int not null, defense int not null, health int not null, vision int not null, movement int not null);
create table abilities (id serial primary key, name varchar(30) not null, class varchar(255) not null, description varchar(255) not null, damage int not null, status varchar(30) not null, mana_cost int not null, range int not null, effect_duration int not null, effect_area int not null);
create table monsters (id serial primary key, name varchar(30) not null, class varchar(255) not null, attack int not null, defense int not null, health int not null, vision int, movement int not null, rarity int not null, constraint pk_rarity foreign key (rarity) references rarities(id) on update cascade on delete cascade);
create table traps (id serial primary key, name varchar(30) not null, class varchar(255) not null, damage int not null, trigger_radius int not null, rarity int not null, constraint pk_rarity foreign key (rarity) references rarities(id) on update cascade on delete cascade);
create table hero_items (id serial primary key, name varchar(30) not null, class varchar(255) not null, type varchar(255) not null, attack_change int not null, defense_change int not null, health_change int not null, movement_change int not null, vision_change int not null, rarity int not null, constraint pk_rarity foreign key (rarity) references rarities(id) on update cascade on delete cascade);
create table maps (id serial primary key, map_name varchar(30) not null, creator_id int not null, width int not null, depth int not null, hero_capacity int not null, constraint pk_player_id foreign key (creator_id) references players(id) on update cascade);
create table tiles (id serial primary key, tile_type varchar(30) not null, is_obstruction boolean default false);
create table levels (hero_id int, level int, upgrade varchar(255) not null, constraint pk_hero_id foreign key (hero_id) references heroes(id) on update cascade on delete cascade, primary key (hero_id, level));
create table packs (id serial primary key, display_name varchar(30) not null, items_in_pack int not null, drops_hero_items boolean  not null default true, drops_monsters boolean  not null default true, drops_traps boolean  not null default true, rare_count int  not null default 0, rare_probability int not null default 0, uncommon_probability int not null default 100);

/*
 * Relation Tables
 */

create table hero_player (player_id int not null, hero_id int not null, level int not null default 1, constraint pk_player_id foreign key (player_id) references players (id) on update cascade on delete cascade, constraint pk_hero_id foreign key (hero_id) references heroes (id) on update cascade on delete cascade);
create table hero_item_player (player_id int not null, hero_item_id int not null, quantity int not null default 0, constraint pk_player_id foreign key (player_id) references players (id) on update cascade on delete cascade, constraint pk_hero_item_id foreign key (hero_item_id) references hero_items (id) on update cascade on delete cascade);
create table monster_player (player_id int not null, monster_id int not null, quantity int not null default 0, constraint pk_player_id foreign key (player_id) references players(id) on update cascade on delete cascade, constraint pk_monster_id foreign key (monster_id) references monsters(id) on update cascade on delete cascade);
create table trap_player (player_id int not null, trap_id int not null, quantity int not null default 0, constraint pk_player_id foreign key (player_id) references players(id) on update cascade on delete cascade, constraint pk_trap_id foreign key (trap_id) references traps(id) on update cascade on delete cascade);
create table map_player (player_id int not null, map_id int not null, constraint pk_player_id foreign key (player_id) references players(id) on update cascade on delete cascade, constraint pk_map_id foreign key (map_id) references maps(id) on update cascade on delete cascade);
create table tile_map (map_id int not null, tile_id int not null, x int not null, y int not null, is_hero_spawn boolean default false, rotation int not null default 0, constraint pk_map_id foreign key (map_id) references maps(id) on update cascade on delete cascade, constraint pk_tile_id foreign key (tile_id) references tiles(id) on update cascade on delete cascade);
create table ability_hero (ability_id int not null, hero_id int not null, constraint pk_hero_id foreign key (hero_id) references heroes(id) on update cascade on delete cascade, constraint pk_ability_id foreign key (ability_id) references abilities(id) on update cascade on delete cascade);
create table ability_monster (ability_id int not null, monster_id int not null, constraint pk_monster_id foreign key (monster_id) references monsters(id) on update cascade on delete cascade, constraint pk_ability_id foreign key (ability_id) references abilities(id) on update cascade on delete cascade);
