insert into heroes (class,attack,defense,health,movement,vision) values ('warrior',10,10,70,5,3);
insert into heroes (class,attack,defense,health,movement,vision) values ('rogue',10,0,50,7,4);
insert into heroes (class,attack,defense,health,movement,vision) values ('mage',15,0,50,7,3);
insert into heroes (class,attack,defense,health,movement,vision) values ('warriorrogue',10,10,50,5,4);
insert into heroes (class,attack,defense,health,movement,vision) values ('warriormage',15,0,50,5,4);
insert into heroes (class,attack,defense,health,movement,vision) values ('roguemage',10,5,50,7,3);
insert into tiles (tile_type,is_obstruction) values ('dirt',false),('wall',true),('sand',false),('water',true),('rock',false),('empty',true),('default',false);
insert into rarities (display_name) values ('common'),('heroic'),('mythic');

insert into players (username,password,currency,is_dev) values ('SYSTEM','SYSTEM',10000,true);
