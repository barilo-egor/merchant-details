package net.rcetech.md.integrations.platform.bridgepay;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
@Slf4j
public enum Status implements MerchantOrderStatus {
    NEW("new", "Инвойс только что создан."),
    PAID("paid", "Инвойс был оплачен."),
    CANCELED("canceled", "Инвойс был отменен."),
    EXPIRED("expired", "Инвойс истек."),
    DISPUTE("dispute", "Инвойс находится в стадии спора.");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status getByValue(String value) {
        for (Status status : Status.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
