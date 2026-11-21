package net.rcetech.md.integrations.platform.onlypays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    WAITING("waiting", "Ожидает оплаты"),
    CANCELLED("cancelled", "Отменен"),
    FINISHED("finished", "Завершен");

    private final String value;

    private final String description;

    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static class Deserializer extends ValueDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) {
            return Status.fromValue(p.getValueAsString());
        }
    }
}
