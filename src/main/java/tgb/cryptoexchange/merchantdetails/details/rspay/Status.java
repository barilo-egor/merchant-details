package tgb.cryptoexchange.merchantdetails.details.rspay;

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
    AVAILABLE("available", "Реквизиты доступны"),
    PENDING("pending", "Ожидает оплаты"),
    PROCESSING("processing", "В обработке"),
    SUCCESS("success", "Успешно оплачена"),
    FAILED("failed", "Отклонена/ошибка"),
    CANCELLED("cancelled", "Инвойс отменен"),
    NO_REQUISITES("no_requisites", "Реквизиты не были получены"),
    REFUNDED("refunded", "Возврат"),
    PARTIAL_REFUNDED("partial_refunded", "Частичный возврат");


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
