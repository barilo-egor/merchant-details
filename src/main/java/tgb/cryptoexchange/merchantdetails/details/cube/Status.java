package tgb.cryptoexchange.merchantdetails.details.cube;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    ACCEPTED("Платеж принят"),
    ERROR("Ошибка при обработке"),
    SUCCESS("Платеж успешно завершен"),
    APPEAL("Создана апелляция");

    private final String description;

    public static class Deserializer extends ValueDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Status.valueOf(jsonParser.getString());
        }
    }

}
