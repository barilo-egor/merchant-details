create table smack_pay_client_id
(
    client_id varchar(255) not null
        primary key,
    user_id   varchar(255) null,
    constraint uk_smack_pay_client_id_user_id
        unique (user_id)
);
