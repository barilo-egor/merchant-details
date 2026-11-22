package net.rcetech.md.integrations.platform.smackpay;

import java.util.Optional;

/**
 * Соответствия наш id (OrderCreationRequest.userId) -> client_id, отправляемый в SmackPay как client_id.
 * Хранятся в таблице smack_pay_client_id (md-domain): строка ищется по user_id,
 * если её нет — под пользователя занимается свободная строка с пустым user_id.
 * Реализация должна быть устойчива к одновременной работе нескольких инстансов.
 */
public interface SmackPayClientIds {

    Optional<String> findOrAssignClientId(String userId);
}
