-- apply changes
create table rc_permission (
  id                            integer auto_increment not null,
  group_                        varchar(255) not null,
  permission                    varchar(255) not null,
  world                         varchar(255),
  comment                       longtext,
  constraint pk_rc_permission primary key (id)
);

create table rc_permission_group_member (
  id                            integer auto_increment not null,
  player                        varchar(40) not null,
  group_                        varchar(255) not null,
  world                         varchar(255),
  constraint pk_rc_permission_group_member primary key (id)
);

