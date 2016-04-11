/*
 * Entity Tables
 */
create table if not exists rarities (id serial primary key, display_name varchar(30));

create table if not exists players (id serial primary key, username varchar(16) unique, password varchar(255), currency int, is_dev boolean);

create table if not exists use_patterns (id serial primary key, inputs int default 1, rotatable boolean default true);
create table if not exists use_pattern_tiles (pattern_id int, x int, y int, effect int, primary key (pattern_id, x, y));
create table if not exists hero_items (id serial primary key, name varchar(30) not null, class varchar(255) not null, image varchar(255) not null, description varchar(255) not null,type varchar(255) not null, attack_change int not null, defense_change int not null, health_change int not null, movement_change int not null, vision_change int not null, use_pattern_id int not null, use_range int default 0, rarity_id int not null, constraint pk_rarity foreign key (rarity_id) references rarities(id) on update cascade on delete cascade, constraint pk_use_pattern foreign key (use_pattern_id) references use_patterns(id) on update cascade on delete cascade);
create table if not exists heroes (id serial primary key, class varchar(255) not null, attack int not null, defense int not null, health int not null, vision int not null, movement int not null, default_weapon int not null, constraint pk_default_weapon foreign key (default_weapon) references hero_items(id));
create table if not exists abilities (id serial primary key, name varchar(30) not null, class varchar(255) not null, description varchar(255) not null, damage int not null, status varchar(30) not null, mana_cost int not null, range int not null, effect_duration int not null, use_pattern_id int not null, constraint pk_use_pattern foreign key (use_pattern_id) references use_patterns(id) on update cascade on delete cascade);
create table if not exists monsters (id serial primary key, name varchar(30) not null, class varchar(255) not null, attack int not null, defense int not null, health int not null, vision int, movement int not null,rarity_id int not null, constraint pk_rarity foreign key (rarity_id) references rarities(id) on update cascade on delete cascade);
create table if not exists traps (id serial primary key, name varchar(30) not null, class varchar(255) not null, damage int not null, trigger_radius int not null, use_pattern_id int not null, rarity_id int not null, constraint pk_rarity foreign key (rarity_id) references rarities(id) on update cascade on delete cascade, constraint pk_use_pattern foreign key (use_pattern_id) references use_patterns(id) on update cascade on delete cascade);
create table if not exists maps (id serial primary key, map_name varchar(30) not null, creator_id int not null, width int not null, depth int not null, hero_capacity int not null, constraint pk_player_id foreign key (creator_id) references players(id) on update cascade);
create table if not exists tiles (id serial primary key, tile_type varchar(30) not null, is_obstruction boolean default false);
create table if not exists levels (hero_id int, experience int, level int, upgrade varchar(255) not null, constraint pk_hero_id foreign key (hero_id) references heroes(id) on update cascade on delete cascade, primary key (hero_id, level));
create table if not exists packs (id serial primary key, display_name varchar(30) not null, items_in_pack int not null, drops_hero_items boolean  not null default true, drops_monsters boolean  not null default true, drops_traps boolean  not null default true, rare_count int  not null default 0, rare_probability int not null default 0, uncommon_probability int not null default 100);

/*
 * Relation Tables
 */

create table if not exists  hero_player (player_id int not null, hero_id int not null, hero_uuid varchar(255) not null unique, experience bigint not null default 0, weapon_id int not null, equipment_id int not null, constraint pk_player_id foreign key (player_id) references players (id) on update cascade on delete cascade, constraint pk_hero_id foreign key (hero_id) references heroes (id) on update cascade on delete cascade, constraint pk_weapon_id foreign key (weapon_id) references hero_items(id) on update cascade on delete cascade,constraint pk_equipment_id foreign key (equipment_id) references hero_items(id) on update cascade on delete cascade, primary key (hero_id, player_id));
create table if not exists  hero_item_player (player_id int not null, hero_item_id int not null, quantity int not null default 0, constraint pk_player_id foreign key (player_id) references players (id) on update cascade on delete cascade, constraint pk_hero_item_id foreign key (hero_item_id) references hero_items (id) on update cascade on delete cascade);
create table if not exists  monster_player (player_id int not null, monster_id int not null, quantity int not null default 0, constraint pk_player_id foreign key (player_id) references players(id) on update cascade on delete cascade, constraint pk_monster_id foreign key (monster_id) references monsters(id) on update cascade on delete cascade);
create table if not exists  trap_player (player_id int not null, trap_id int not null, quantity int not null default 0, constraint pk_player_id foreign key (player_id) references players(id) on update cascade on delete cascade, constraint pk_trap_id foreign key (trap_id) references traps(id) on update cascade on delete cascade);
create table if not exists  tile_map (map_id int not null, tile_id int not null, x int not null, y int not null, is_hero_spawn boolean default false, is_architect_spawn boolean default false, is_objective_spawn boolean default false, rotation int not null default 0, constraint pk_map_id foreign key (map_id) references maps(id) on update cascade on delete cascade, constraint pk_tile_id foreign key (tile_id) references tiles(id) on update cascade on delete cascade);
create table if not exists  ability_hero (ability_id int not null, hero_id int not null, constraint pk_hero_id foreign key (hero_id) references heroes(id) on update cascade on delete cascade, constraint pk_ability_id foreign key (ability_id) references abilities(id) on update cascade on delete cascade);
create table if not exists  ability_monster (ability_id int not null, monster_id int not null, constraint pk_monster_id foreign key (monster_id) references monsters(id) on update cascade on delete cascade, constraint pk_ability_id foreign key (ability_id) references abilities(id) on update cascade on delete cascade);
