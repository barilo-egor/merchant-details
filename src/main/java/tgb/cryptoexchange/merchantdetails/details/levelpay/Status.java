package tgb.cryptoexchange.merchantdetails.details.levelpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    SUCCESS("success", "Успешно"),
    FAIL("fail", "Ошибка"),
    PENDING("pending", "Отправка");

    private final String value;

    private final String description;

    public static Status fromValue(String value ) {
        Status[] values = Status.values();
        for (Status status : values ) {
            if (status.getValue().equals( value ) ) {
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
