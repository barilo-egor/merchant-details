package tgb.cryptoexchange.merchantdetails.details.pulsar;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status {
    CREATED("created", "Создан (без метода)"),
    PENDING("pending", "Ожидает оплаты"),
    DISPUTE("dispute", "Спор"),
    IN_CHECK("in_check", "На проверке"),
    FINISHED("finished", "Завершен"),
    CANCELED("canceled", "Отменен"),
    EXPIRED("expired", "Просрочен"),
    FAILED("failed", "Ошибка");

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

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.fromValue(jsonParser.getValueAsString());
        }
    }
}
