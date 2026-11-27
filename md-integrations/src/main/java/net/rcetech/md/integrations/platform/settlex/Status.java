package net.rcetech.md.integrations.platform.settlex;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("Транзакция создана и ожидает обработки"),
    IN_PROGRESS("Транзакция в процессе обработки трейдером"),
    READY("Транзакция готова к завершению"),
    MILK("Специальный статус обработки"),
    DISPUTE("По транзакции открыт спор"),
    EXPIRED("Время на выполнение транзакции истекло"),
    CANCELED("Транзакция отменена");

    private final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        return Status.valueOf(v);
    }
}
