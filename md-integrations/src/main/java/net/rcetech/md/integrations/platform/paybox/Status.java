package net.rcetech.md.integrations.platform.paybox;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PAID("paid", "Оплачено"),
    UNDERPAID("underpaid", "Недоплачено"),
    OVERPAID("overpaid", "Переплачено"),
    PROCESS("process", "Ожидает оплаты"),
    EXPIRED("expired", "Просрочена"),
    CANCEL("cancel", "Отменена мерчантом"),
    ERROR("error", "Произошла ошибка при создании платежа");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status fromValue(String value) {
        for (Status payPointsStatus : Status.values()) {
            if (payPointsStatus.getValue().equals(value)) {
                return payPointsStatus;
            }
        }
        return null;
    }
}
