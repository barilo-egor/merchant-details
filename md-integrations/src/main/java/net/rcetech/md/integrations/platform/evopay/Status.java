package net.rcetech.md.integrations.platform.evopay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("Создана"),
    IN_PROCESS("В процессе"),
    EXPIRE("Истек"),
    SUCCESS("Успешно"),
    CANCEL("Отменен"),
    APPEAL("В споре");

    final String description;
}
