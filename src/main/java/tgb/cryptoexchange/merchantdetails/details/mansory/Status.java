package tgb.cryptoexchange.merchantdetails.details.mansory;

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
    WAITING_PAYMENT("waiting_payment", "Ожидает оплаты от клиента"),
    COMPLETED("completed", "Оплачен, средства зачислены"),
    EXPIRED("expired", "Просрочен. Можно создать апелляцию"),
    CANCELED("canceled", "Отменен мерчантом"),
    PENDING("pending", "Ожидает решения саппорта"),
    RESOLVED_TRADER("resolved_trader", "Решено в пользу трейдера"),
    RESOLVED_MERCHANT("resolved_merchant", "Решено в пользу мерчанта");

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