alter table merchant_config
    rename index UK6w1h6kwdiuw7wjj3y5omgmguj to uk_merchant_config_merchant,
    rename index UKcx9xy5atf9oi7kbh6etbcflk3 to uk_merchant_config_merchant_order;

alter table merchant_config_confirm_configs
    drop foreign key FK7i8sf6gtr7wjt02cb7s6il8kn,
    drop foreign key FKmb093pq0valerftvpo9q99hhi,
    rename index UKgy814ewckompnk6or7vkbtlt6 to uk_mc_confirm_configs_confirm_config,
    rename index FKmb093pq0valerftvpo9q99hhi to idx_mc_confirm_configs_merchant_config;

alter table merchant_config_confirm_configs
    add constraint fk_mc_confirm_configs_confirm_config
        foreign key (confirm_configs_id) references auto_confirm_config (id),
    add constraint fk_mc_confirm_configs_merchant_config
        foreign key (merchant_config_id) references merchant_config (id);

alter table merchant_config_success_statuses
    drop foreign key FK4mbak23c6ehxmk0vqrd4nbdbb,
    drop foreign key FKtrjf5vs5u9funigik2x70bh86,
    rename index UK3qwy8wt3b2fosge3oyr9y8p8 to uk_mc_success_statuses_success_status,
    rename index FKtrjf5vs5u9funigik2x70bh86 to idx_mc_success_statuses_merchant_config;

alter table merchant_config_success_statuses
    add constraint fk_mc_success_statuses_success_status
        foreign key (success_statuses_id) references merchant_success_status (id),
    add constraint fk_mc_success_statuses_merchant_config
        foreign key (merchant_config_id) references merchant_config (id);

alter table variable
    drop index `type`,
    rename index uc_variable_config_type to uk_variable_type_config_type;

alter table api_merchant_config
    modify owner_id binary(16) not null,
    add constraint uk_api_merchant_config_owner_id_merchant
        unique (owner_id, merchant);

alter table auto_confirm_config
    modify auto_confirm_type varchar(32) null,
    modify crypto_currency varchar(32) null,
    modify delivery_type varchar(32) null;

alter table merchant_config
    modify required_receipt varchar(32) null;

alter table variable
    modify config_type varchar(32) null;

create table merchant_config_seq_new
(
    next_val bigint not null
        primary key
);

insert into merchant_config_seq_new (next_val)
select max(next_val)
from merchant_config_seq;

drop table merchant_config_seq;

rename table merchant_config_seq_new to merchant_config_seq;
