package tgb.cryptoexchange.merchantdetails.details.viatrum;

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
    CREATED("CREATED", "Заявка создана"),
    PENDING("PENDING", "В ожидании реквизитов"),
    PROCESSING("PROCESSING", "Заявка обрабатывается"),
    COMPLETED("COMPLETED", "Заявка выполнена"),
    TIMEOUT("TIMEOUT", "Истекло время ожидания оплаты"),
    CANCELLED("CANCELLED", "Заявка отменена"),
    ERROR("ERROR", "Ошибка при обработке"),
    INCORRECT_AMOUNT("INCORRECT_AMOUNT", "Некорректная сумма"),
    REFUNDED("REFUNDED", "Возвращен");

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
