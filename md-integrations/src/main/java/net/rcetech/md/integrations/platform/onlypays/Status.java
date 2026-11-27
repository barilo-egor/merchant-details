package net.rcetech.md.integrations.platform.onlypays;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    WAITING("waiting", "Ожидает оплаты"),
    CANCELLED("cancelled", "Отменен"),
    FINISHED("finished", "Завершен");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
