package tgb.cryptoexchange.merchantdetails.details.paycrown;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status {
    NEW("new", "Новый"),
    OPENED("opened", "Открыт"),
    PROCESS("process", "В процессе"),
    CANCELED("canceled", "Заказ отменен"),
    EXPIRED("expired", "Срок истек"),
    CLOSED("closed", "Успешно исполнен");

    private final String value;

    private final String description;

    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.value.equals(value)) {
                return status;
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
