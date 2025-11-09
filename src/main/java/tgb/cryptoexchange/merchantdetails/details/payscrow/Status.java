package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status {
    UNPAID("Unpaid", "Ожидает оплаты"),
    COMPLETED("Completed", "Успешно завершено"),
    CANCELED_BY_TIMEOUT("CanceledByTimeout", "Отменен по таймауту"),
    CANCELED_BY_SERVICE("CanceledByService", "Ручная отмена");

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
