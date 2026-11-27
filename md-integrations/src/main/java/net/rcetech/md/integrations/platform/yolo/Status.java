package net.rcetech.md.integrations.platform.yolo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("Создана"),
    PENDING("В ожидании"),
    COMPLETED("Завершена"),
    FAILED("Ошибка"),
    CANCELED("Отменена"),
    EXPIRED("Истек срок"),
    DISPUTE("Апелляция");

    private final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        return Status.valueOf(v);
    }

}
