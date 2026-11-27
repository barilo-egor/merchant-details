package net.rcetech.md.integrations.platform.paylee;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("pending", "В ожидании"),
    CANCELED("canceled", "Отменено"),
    COMPLETED("completed", "Завершено"),
    TIMEOUT("timeout", "Таймаут");

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
