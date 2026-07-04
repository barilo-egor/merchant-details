package tgb.cryptoexchange.merchantdetails.details.zpay;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

import java.io.IOException;

@AllArgsConstructor
@Getter
@Slf4j
public enum Status implements MerchantOrderStatus {
    INITIATED("initiated", "Создан"),
    PAID("paid", "Оплачен"),
    DISPUTE("dispute", "В споре"),
    COMPLETED("completed", "Выполнен"),
    CANCELLED("cancelled", "Отменен"),
    REMOVED("removed", "Удален");

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