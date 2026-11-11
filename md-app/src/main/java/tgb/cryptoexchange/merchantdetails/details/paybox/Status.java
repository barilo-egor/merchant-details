package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

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

    public static class Deserializer extends ValueDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) {
            return Status.fromValue(p.getValueAsString());
        }
    }
}
