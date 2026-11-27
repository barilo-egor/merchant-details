package net.rcetech.md.integrations.platform.bitzone;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("В ожидании"),
    ACTIVE("Активный"),
    CHECKING("Проверка"),
    DISPUTE("Спор"),
    RE_CALCULATION("Перерасчёт"),
    CLOSED("Закрыт"),
    CANCELED("Отменён");

    private final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        return Status.valueOf(v.toUpperCase());
    }
}
