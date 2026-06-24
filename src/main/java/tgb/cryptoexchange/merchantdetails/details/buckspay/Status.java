package tgb.cryptoexchange.merchantdetails.details.buckspay;

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
    CREATED(1, "Инвойс создан, реквизиты оплаты не выбраны покупателем"),
    WAITING_FOR_PAYMENT(2, "Покупатель выбрал способ оплаты, ожидание оплаты от покупателя"),
    WAITING_FOR_TRADER(3, "Ожидание подтверждения оплаты трейдером"),
    PAID(4, "Инвойс успешно завершен"),
    TIMEOUT(5, "Время на оплату истекло"),
    CANCELLED(6, "Инвойс отменен"),
    INCORRECT_AMOUNT(7, "Изменена сумма сделки"),
    RESTORED(8, "Сделка восстановлена"),
    REVERTED(9, "Выполнен откат сделки из PAID");


    private final Integer value;

    private final String description;

    public static Status fromValue(Integer v) {
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
            return Status.fromValue(jsonParser.getValueAsInt());
        }
    }
}
