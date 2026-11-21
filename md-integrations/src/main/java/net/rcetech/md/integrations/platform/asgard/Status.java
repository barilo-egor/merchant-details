package net.rcetech.md.integrations.platform.asgard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("created", "Создан"),
    PENDING("pending", "Ожидает оплаты"),
    DISPUTE("dispute", "В споре"),
    IN_CHECK("in_check", "На проверке"),
    FINISHED("finished", "Завершен"),
    CANCELED("canceled", "Отменен"),
    EXPIRED("expired", "Истек"),
    FAILED("failed", "Ошибка");

    final String value;

    final String description;

    public static Status fromValue(String v) {
        for (Status status : Status.values()) {
            if (status.value.equals(v)) {
                return status;
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
