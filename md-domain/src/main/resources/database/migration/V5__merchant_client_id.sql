-- Единая таблица соответствий "наш id -> client_id мерчанта" вместо таблицы на площадку.
-- Данные переносятся из smack_pay_client_id и can_pay_client_id.
create table merchant_client_id
(
    client_id varchar(255) not null,
    merchant  varchar(50)  not null,
    user_id   varchar(255) null,
    primary key (client_id, merchant),
    constraint uk_merchant_client_id_user_id unique (merchant, user_id)
);
insert into merchant_client_id (client_id, merchant, user_id)
    select client_id, 'SMACK_PAY', user_id from smack_pay_client_id;
insert into merchant_client_id (client_id, merchant, user_id)
    select client_id, 'CANPAY', user_id from can_pay_client_id;
-- Старые таблицы пока НЕ удаляются (expand-contract): откат релиза требует их
-- на месте, пока прежний код читает smack_pay_client_id/can_pay_client_id.
-- Удалить отдельной миграцией следующего релиза после стабилизации на проде.
