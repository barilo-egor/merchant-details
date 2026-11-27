package net.rcetech.md.integrations.platform.cube;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    ACCEPTED("Платеж принят"),
    ERROR("Ошибка при обработке"),
    SUCCESS("Платеж успешно завершен"),
    APPEAL("Создана апелляция");

    private final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        return Status.valueOf(v);
    }

}
