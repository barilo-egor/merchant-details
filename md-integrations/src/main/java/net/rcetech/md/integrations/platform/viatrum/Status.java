package net.rcetech.md.integrations.platform.viatrum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("CREATED", "Заявка создана"),
    PENDING("PENDING", "В ожидании реквизитов"),
    PROCESSING("PROCESSING", "Заявка обрабатывается"),
    COMPLETED("COMPLETED", "Заявка выполнена"),
    TIMEOUT("TIMEOUT", "Истекло время ожидания оплаты"),
    CANCELLED("CANCELLED", "Заявка отменена"),
    ERROR("ERROR", "Ошибка при обработке"),
    INCORRECT_AMOUNT("INCORRECT_AMOUNT", "Некорректная сумма"),
    REFUNDED("REFUNDED", "Возвращен");

    private final String value;

    private final String description;

    public static Status fromValue(String v) {
        for (Status c : Status.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }

    public static class Deserializer extends ValueDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Status.fromValue(jsonParser.getValueAsString());
        }
    }
}
