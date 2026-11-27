package net.rcetech.md.integrations.platform.goatx;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("pending", "Отправлен"),
    CONFIRMED("confirmed", "Подтвержден"),
    DISPUTE("dispute", "Спор"),
    CANCELED("canceled", "Отменен");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status fromValue(final String value) {
        for (Status status : Status.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
