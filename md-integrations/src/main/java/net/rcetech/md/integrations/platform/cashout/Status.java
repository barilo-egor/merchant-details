package net.rcetech.md.integrations.platform.cashout;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("pending", "Создано"),
    PROCESSING("processing", "Платеж обрабатывается."),
    COMPLETED("completed", "Подтверждено"),
    FAILED("failed", "Платеж не удался"),
    CANCELLED("cancelled", "Отменено"),
    DISPUTE("dispute", "Начался спор"),
    COMPLETED_AFTER_DISPUTE("completed_after_dispute", "Подтверждено после спора"),
    FAILED_AFTER_DISPUTE("failed_after_dispute", "Неудача после спора");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        for (Status c : Status.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
