package net.rcetech.md.integrations.platform.wat;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    AWAITING_PAYMENT("awaiting_payment", "Ожидает оплаты"),
    FINISHED("finished", "Завершён"),
    CANCELED("canceled", "Отменён"),
    CLAIM("claim", "Открыта претензия");

    final String value;

    final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        for (Status status : Status.values()) {
            if (status.value.equals(v)) {
                return status;
            }
        }
        return null;
    }
}