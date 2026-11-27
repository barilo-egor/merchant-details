package net.rcetech.md.integrations.platform.rspay;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    AVAILABLE("available", "Реквизиты доступны"),
    PENDING("pending", "Ожидает оплаты"),
    PROCESSING("processing", "В обработке"),
    SUCCESS("success", "Успешно оплачена"),
    FAILED("failed", "Отклонена/ошибка"),
    CANCELLED("cancelled", "Инвойс отменен"),
    NO_REQUISITES("no_requisites", "Реквизиты не были получены"),
    REFUNDED("refunded", "Возврат"),
    PARTIAL_REFUNDED("partial_refunded", "Частичный возврат");


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
