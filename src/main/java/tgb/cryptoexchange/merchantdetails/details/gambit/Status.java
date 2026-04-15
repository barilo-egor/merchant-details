package tgb.cryptoexchange.merchantdetails.details.gambit;

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
    PENDING("pending", "Ожидает оплаты"),
    PROCESSING("processing", "В обработке"),
    AWAITING_FUNDS("awaiting_funds", "Ожидание поступления"),
    SUCCEEDED("succeeded", "Оплачен"),
    FAILED("failed", "Ошибка"),
    CANCELLED("cancelled", "Отменен"),
    REFUNDED("refunded", "Возврат");

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
