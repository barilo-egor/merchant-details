package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
@Slf4j
public enum Status implements MerchantOrderStatus {
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

    public static class Deserializer extends ValueDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Status.getByValue(jsonParser.getValueAsString());
        }
    }
}
