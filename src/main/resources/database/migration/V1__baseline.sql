create table api_merchant_config
(
    id             bigint      not null auto_increment
        primary key,
    is_on          bit         null,
    max_amount     int         null,
    merchant       varchar(32) not null,
    merchant_order int         not null,
    min_amount     int         null,
    owner_id       binary(16)  null
);

create table auto_confirm_config
(
    id                bigint                                       not null
        primary key,
    auto_confirm_type enum ('ADD_TO_POOL', 'AUTO_WITHDRAWAL')      null,
    crypto_currency   enum ('BITCOIN', 'LITECOIN', 'MONERO', 'USDT') null,
    delivery_type     enum ('STANDARD', 'VIP')                     null
);

create table auto_confirm_config_seq
(
    next_val bigint not null
        primary key
);

create table merchant_config
(
    id                    bigint                                not null
        primary key,
    group_chat_id         bigint                                null,
    is_auto_withdrawal_on bit                                   null,
    is_on                 bit                                   null,
    max_amount            int                                   null,
    merchant              varchar(32)                           not null,
    merchant_order        int                                   not null,
    min_amount            int                                   null,
    min_deals_count       int default 0                         not null,
    required_receipt      enum ('ANY', 'NOT_REQUIRED', 'PDF')   null,
    constraint UK6w1h6kwdiuw7wjj3y5omgmguj
        unique (merchant),
    constraint UKcx9xy5atf9oi7kbh6etbcflk3
        unique (merchant_order)
);

create table merchant_config_seq
(
    id       int    not null auto_increment
        primary key,
    next_val bigint null
);

create table merchant_success_status
(
    id     bigint       not null
        primary key,
    status varchar(255) null
);

create table merchant_success_status_seq
(
    next_val bigint not null
        primary key
);

create table merchant_config_confirm_configs
(
    merchant_config_id bigint not null,
    confirm_configs_id bigint not null,
    constraint UKgy814ewckompnk6or7vkbtlt6
        unique (confirm_configs_id),
    constraint FK7i8sf6gtr7wjt02cb7s6il8kn
        foreign key (confirm_configs_id) references auto_confirm_config (id),
    constraint FKmb093pq0valerftvpo9q99hhi
        foreign key (merchant_config_id) references merchant_config (id)
);

create index FKmb093pq0valerftvpo9q99hhi
    on merchant_config_confirm_configs (merchant_config_id);

create table merchant_config_success_statuses
(
    merchant_config_id  bigint not null,
    success_statuses_id bigint not null,
    constraint UK3qwy8wt3b2fosge3oyr9y8p8
        unique (success_statuses_id),
    constraint FK4mbak23c6ehxmk0vqrd4nbdbb
        foreign key (success_statuses_id) references merchant_success_status (id),
    constraint FKtrjf5vs5u9funigik2x70bh86
        foreign key (merchant_config_id) references merchant_config (id)
);

create index FKtrjf5vs5u9funigik2x70bh86
    on merchant_config_success_statuses (merchant_config_id);

create table variable
(
    id          bigint              not null
        primary key,
    type        varchar(64)         not null,
    value       varchar(255)        null,
    config_type enum ('API', 'BOT') null,
    constraint type
        unique (type),
    constraint uc_variable_config_type
        unique (type, config_type)
);

create table variable_seq
(
    next_val bigint not null
        primary key
);

insert into auto_confirm_config_seq (next_val) values (1);
insert into merchant_config_seq (next_val) values (1);
insert into merchant_success_status_seq (next_val) values (1);
insert into variable_seq (next_val) values (1);
