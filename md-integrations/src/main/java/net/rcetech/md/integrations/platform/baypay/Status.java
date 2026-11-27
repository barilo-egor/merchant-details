package net.rcetech.md.integrations.platform.baypay;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    ACTIVE("Активна", "Активна"),
    COMPLETED("Завершена", "Завершена"),
    CANCELLED("Отменена", "Отменена");

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