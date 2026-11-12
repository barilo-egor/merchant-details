package net.rcetech.md.integrations.platform.prismapay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("Создана"),
    CANCELLED("Отменено"),
    DISPUTE("В споре"),
    SUCCESS("Успешно"),
    WAITING_ADMIN_APPROVAL("Ожидает одобрения администратора"),
    EXPIRED("Просрочена"),
    NO_REQUISITES("Отсутствуют реквизиты");

    final String description;
}
