package net.rcetech.md.integrations.platform.gambit;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("pending", "Ожидает оплаты"),
    PROCESSING("processing", "В обработке"),
    AWAITING_FUNDS("awaiting_funds", "Ожидание поступления"),
    SUCCEEDED("succeeded", "Оплачен"),
    FAILED("failed", "Ошибка"),
    CANCELLED("cancelled", "Отменен"),
    REFUNDED("refunded", "Возврат");

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
