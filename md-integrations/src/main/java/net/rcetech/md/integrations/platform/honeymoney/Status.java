package net.rcetech.md.integrations.platform.honeymoney;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("В процессе"),
    DENIED("Отклонено"),
    SUCCESSFUL("Успешно");

    private final String description;
}
