package tgb.cryptoexchange.merchantdetails.details.ezepay;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status {
    CHOOSING_METHOD(0, "На стадии выбора способа оплаты"),
    AGREEMENT(1, "Соглашение с информацией о заявке"),
    SHOWING_DETAILS(2, "Показ реквизитов, совершение платежа"),
    PAYMENT_MADE(3, "Перевод выполнен, ожидание подтверждения платежа"),
    SUCCESSFUL(4, "Средства поступили, успешно"),
    CANCELED(5, "Заявка отменена");

    private final int code;
    private final String description;

    public static Status fromCode(int code) {
        for (Status status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown EzePayStatus code: " + code);
    }

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.fromCode(Integer.parseInt(jsonParser.getValueAsString()));
        }
    }
}
