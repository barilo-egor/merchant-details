package tgb.cryptoexchange.merchantdetails.details.goatx;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("pending", "Отправлен"),
    CONFIRMED("confirmed", "Подтвержден"),
    DISPUTE("dispute", "Спор"),
    CANCELED("canceled", "Отменен");

    private final String value;

    private final String description;

    public static Status fromValue(final String value) {
        for (Status status : Status.values()) {
            if (status.value.equals(value)) {
                return status;
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
