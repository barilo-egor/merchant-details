package tgb.cryptoexchange.merchantdetails.details.appexbit;

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
    CREATED(0, "Создана"),
    WAITING(1, "Ожидает"),
    ACCEPTED(2, "Принята"),
    COMPLETED(3, "Завершена"),
    EXPIRED(4, "Просрочена"),
    VALIDATION(5, "Валидация"),
    LOST(6, "Потеряна"),
    COMPLETED_BY_CLIENT(7, "Завершена клиентом"),
    DISPUTE(8, "Спор"),
    VERIFICATION(9, "Верификация"),
    CANCELLED(10, "Отмена"),
    BLOCKED(11, "Заблокирована"),
    SYSTEM_CANCELLED(12, "Отмена системой"),
    AMOUNT_CHANGED(13, "Изменена сумма"),
    VERIFICATION_CANCELLED(14, "Отмена верификации"),
    PAYMENT_METHOD_SELECTION(15, "Выбор метода оплаты пользователем");

    private final int value;

    private final String description;

    public static Status fromValue(final int value) {
        for (Status status : Status.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Status.fromValue(p.getIntValue());
        }
    }
}
