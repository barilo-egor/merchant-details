package net.rcetech.md.integrations.platform.manypay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("Создан, ожидает подтверждения"),
    WAITING_PAYMENT("Ожидает оплаты"),
    PAID("Оплата получена"),
    CANCELLED("Отменён"),
    EXPIRED("Истёк");

    private final String description;

}
