package tgb.cryptoexchange.merchantdetails.details.paybox;

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
    PAID("paid", "Оплачено"),
    UNDERPAID("underpaid", "Недоплачено"),
    OVERPAID("overpaid", "Переплачено"),
    PROCESS("process", "Ожидает оплаты"),
    EXPIRED("expired", "Просрочена"),
    CANCEL("cancel", "Отменена мерчантом"),
    ERROR("error", "Произошла ошибка при создании платежа");

    private final String value;

    private final String description;

    public static Status fromValue(String value) {
        for (Status payPointsStatus : Status.values()) {
            if (payPointsStatus.getValue().equals(value)) {
                return payPointsStatus;
            }
        }
        return null;
    }

    public static class Deserializer extends JsonDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Status.fromValue(p.getValueAsString());
        }
    }
}
