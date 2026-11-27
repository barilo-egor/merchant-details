package net.rcetech.md.integrations.platform.tronex;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("created", "Ожидает оплаты"),
    IN_PROGRESS("in_progress", "В процессе"),
    COMPLETED("completed", "Завершен"),
    CANCELLED("cancelled", "Отменён");

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