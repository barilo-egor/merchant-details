package net.rcetech.md.integrations.platform.payscrow;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    UNPAID("Unpaid", "Ожидает оплаты"),
    COMPLETED("Completed", "Успешно завершено"),
    CANCELED_BY_TIMEOUT("CanceledByTimeout", "Отменен по таймауту"),
    CANCELED_BY_SERVICE("CanceledByService", "Ручная отмена");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        for (Status c: Status.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }

}
