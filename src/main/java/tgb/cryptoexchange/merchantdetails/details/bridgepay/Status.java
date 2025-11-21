package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@AllArgsConstructor
@Getter
@Slf4j
public enum Status {
    NEW("new", "Инвойс только что создан."),
    PAID("paid", "Инвойс был оплачен."),
    CANCELED("canceled", "Инвойс был отменен."),
    EXPIRED("expired", "Инвойс истек."),
    DISPUTE("dispute", "Инвойс находится в стадии спора.");

    private final String value;

    private final String description;

    public static Status getByValue(String value) {
        for (Status status : Status.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.getByValue(jsonParser.getValueAsString());
        }
    }
}
