package tgb.cryptoexchange.merchantdetails.details.baypay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    ACTIVE("Активна", "Активна"),
    COMPLETED("Завершена", "Завершена"),
    CANCELLED("Отменена", "Отменена");

    final String value;

    final String description;

    public static Status fromValue(String v) {
        for (Status status : Status.values()) {
            if (status.value.equals(v)) {
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