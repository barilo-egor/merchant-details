package tgb.cryptoexchange.merchantdetails.details.pandapay;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status {
    PENDING("pending", "Создан"),
    TRADER_NOT_FOUND("traderNotFound", "Трейдер не найден"),
    TIMEOUT("timeout", "Время вышло"),
    COMPLETED("completed", "Завершен"),
    CANCELED("canceled", "Отменен");

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

    public static class Deserializer extends JsonDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Status.fromValue(p.getValueAsString());
        }
    }
}
