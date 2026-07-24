package tgb.cryptoexchange.merchantdetails.details.studio;

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
    PENDING("pending", "Ордер создан, ожидает обработки"),
    WAITING_PAYMENT("waiting_payment", "Ожидает оплаты"),
    AWAITING_REQUISITES("awaiting_requisites", "Ожидает получения реквизитов"),
    SUCCESS("success", "Оплачен успешно"),
    ERROR("error", "Ошибка при обработке"),
    FAILED("failed", "Отказ в обработке"),
    NO_REQUISITES("no_requisites", "Не удалось получить реквизиты"),
    EXPIRED("expired", "Просрочен");

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
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            return Status.getByValue(jsonParser.getValueAsString());
        }

    }
}
