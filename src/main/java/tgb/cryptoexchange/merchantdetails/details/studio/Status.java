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
    PENDING("Ордер создан, ожидает обработки"),
    WAITING_PAYMENT("Ожидает оплаты"),
    AWAITING_REQUISITES("Ожидает получения реквизитов"),
    SUCCESS("Оплачен успешно"),
    ERROR("Ошибка при обработке"),
    FAILED("Отказ в обработке"),
    NO_REQUISITES("Не удалось получить реквизиты"),
    EXPIRED("Просрочен");

    private final String description;

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.valueOf(jsonParser.getText());
        }
    }
}
