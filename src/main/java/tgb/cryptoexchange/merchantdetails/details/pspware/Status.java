package tgb.cryptoexchange.merchantdetails.details.pspware;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status {
    PROCESSING("processing", "В процессе"),
    CANCELED("canceled", "Отменена"),
    APPEL("appel", "Апелляция"),
    SUCCESS("success", "Завершена"),
    FAILED("failed", "Не завершена");

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
