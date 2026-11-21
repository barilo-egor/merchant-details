package net.rcetech.md.integrations.platform.paylee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("pending", "В ожидании"),
    CANCELED("canceled", "Отменено"),
    COMPLETED("completed", "Завершено"),
    TIMEOUT("timeout", "Таймаут");

    private final String value;

    private final String description;

    public static Status fromValue(String v) {
        for (Status c: Status.values()) {
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
