package net.rcetech.md.integrations.platform.crocopay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@Getter
@AllArgsConstructor
public enum Status implements MerchantOrderStatus {
    PENDING("Ожидание"),
    CANCELLED("Отменен"),
    SUCCESS("Платеж зачислен"),
    DISPUTE("Спор"),
    EXPIRED("Истекший");

    private final String description;

    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.name().equals(value.toUpperCase())) {
                return status;
            }
        }
        return null;
    }

    public static class Deserializer extends ValueDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) {
            return Status.fromValue(p.getValueAsString().toUpperCase());
        }
    }
}
