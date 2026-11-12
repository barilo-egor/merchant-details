package net.rcetech.md.integrations.platform.lotrien;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("Создана"),
    PENDING("В ожидании"),
    IN_PROGRESS("В процессе"),
    SUCCESS("Успешно"),
    UNISSUED("Не создана"),
    EXPIRE("Просрочена"),
    APPEAL("В споре"),
    CANCEL("Отменена");

    final String description;
}
