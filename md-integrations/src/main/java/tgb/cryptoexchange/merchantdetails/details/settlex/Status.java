package tgb.cryptoexchange.merchantdetails.details.settlex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("Транзакция создана и ожидает обработки"),
    IN_PROGRESS("Транзакция в процессе обработки трейдером"),
    READY("Транзакция готова к завершению"),
    MILK("Специальный статус обработки"),
    DISPUTE("По транзакции открыт спор"),
    EXPIRED("Время на выполнение транзакции истекло"),
    CANCELED("Транзакция отменена");

    private final String description;

    public static class Deserializer extends ValueDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Status.valueOf(jsonParser.getString());
        }
    }
}
