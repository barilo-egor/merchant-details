package tgb.cryptoexchange.merchantdetails.details.asgard;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("created", "Создан"),
    PENDING("pending", "Ожидает оплаты"),
    DISPUTE("dispute", "В споре"),
    IN_CHECK("in_check", "На проверке"),
    FINISHED("finished", "Завершен"),
    CANCELED("canceled", "Отменен"),
    EXPIRED("expired", "Истек"),
    FAILED("failed", "Ошибка");

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

    public static class Deserializer extends JsonDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.fromValue(jsonParser.getValueAsString());
        }
    }
}
