package tgb.cryptoexchange.merchantdetails.details.eclipsegate;

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
    PENDING("pending", "Заявка создана, идёт подбор реквизитов"),
    WAITING_PAYMENT("waiting_payment", "Реквизиты выданы, ожидается оплата от пользователя"),
    SUCCESS("success", "Оплата подтверждена, средства начислены на баланс"),
    EXPIRED("expired", "Время на оплату истекло"),
    ERROR("error", "Произошла ошибка при обработке"),
    NO_REQUISITES("no_requisites", "Реквизиты не были получены"),
    CANCELLED("cancelled", "Заявка отменена");

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

