package tgb.cryptoexchange.merchantdetails.details.cashout;

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
    PENDING("pending", "Создано"),
    PROCESSING("processing", "Платеж обрабатывается."),
    COMPLETED("completed", "Подтверждено"),
    FAILED("failed", "Платеж не удался"),
    CANCELLED("cancelled", "Отменено"),
    DISPUTE("dispute", "Начался спор"),
    COMPLETED_AFTER_DISPUTE("completed_after_dispute", "Подтверждено после спора"),
    FAILED_AFTER_DISPUTE("failed_after_dispute", "Неудача после спора");

    private final String value;

    private final String description;

    public static Status fromValue(String v) {
        for (Status c : Status.values()) {
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
